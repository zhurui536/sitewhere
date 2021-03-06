/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.SolrPingResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrInputDocument;

import com.sitewhere.device.event.processor.FilteredOutboundEventProcessor;
import com.sitewhere.spi.SiteWhereException;
import com.sitewhere.spi.device.event.IDeviceAlert;
import com.sitewhere.spi.device.event.IDeviceLocation;
import com.sitewhere.spi.device.event.IDeviceMeasurements;
import com.sitewhere.spi.device.event.processor.IOutboundEventProcessor;
import com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor;

/**
 * {@link IOutboundEventProcessor} implementation that takes saved events and
 * indexes them in Apache Solr for advanced analytics processing.
 * 
 * @author Derek
 */
public class SolrDeviceEventProcessor extends FilteredOutboundEventProcessor {

    /** Static logger instance */
    private static Logger LOGGER = LogManager.getLogger();

    /** Number of documents to buffer before blocking calls */
    private static final int BUFFER_SIZE = 1000;

    /** Interval at which batches are written */
    private static final int BATCH_INTERVAL = 2 * 1000;

    /** Maximum count of documents to send in a batch */
    private static final int MAX_BATCH_SIZE = 200;

    /** Interval by which documents should be committed */
    private static final int COMMIT_INTERVAL = 60 * 1000;

    /** Injected Solr configuration */
    private SiteWhereSolrConfiguration solr;

    /** Bounded queue that holds documents to be processed */
    private BlockingQueue<SolrInputDocument> queue = new ArrayBlockingQueue<SolrInputDocument>(BUFFER_SIZE);

    /** Used to execute Solr indexing in a separate thread */
    /** TODO: Use a better approach for scalability */
    private ExecutorService executor = Executors.newSingleThreadExecutor();

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.sitewhere.device.event.processor.FilteredOutboundEventProcessor#start
     * (com.sitewhere.spi.server.lifecycle.ILifecycleProgressMonitor)
     */
    @Override
    public void start(ILifecycleProgressMonitor monitor) throws SiteWhereException {
	// Required for filters.
	super.start(monitor);

	if (getSolr() == null) {
	    throw new SiteWhereException("No Solr configuration provided to " + getClass().getName());
	}
	try {
	    LOGGER.info("Attempting to ping Solr server to verify availability...");
	    SolrPingResponse response = getSolr().getSolrClient().ping();
	    int pingTime = response.getQTime();
	    LOGGER.info("Solr server location verified. Ping responded in " + pingTime + " ms.");
	} catch (SolrServerException e) {
	    throw new SiteWhereException("Ping failed. Verify that Solr server is available.", e);
	} catch (IOException e) {
	    throw new SiteWhereException("Exception in ping. Verify that Solr server is available.", e);
	}
	LOGGER.info("Solr event processor indexing events to server at: " + getSolr().getSolrServerUrl());
	executor.execute(new SolrDocumentQueueProcessor());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.spi.server.lifecycle.ILifecycleComponent#getLogger()
     */
    @Override
    public Logger getLogger() {
	return LOGGER;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.device.event.processor.FilteredOutboundEventProcessor#
     * onMeasurementsNotFiltered(com.sitewhere.spi.device.event.
     * IDeviceMeasurements)
     */
    @Override
    public void onMeasurementsNotFiltered(IDeviceMeasurements measurements) throws SiteWhereException {
	SolrInputDocument document = SiteWhereSolrFactory.createDocumentFromMeasurements(measurements);
	try {
	    queue.put(document);
	} catch (InterruptedException e) {
	    throw new SiteWhereException("Interrupted during indexing.", e);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.device.event.processor.FilteredOutboundEventProcessor#
     * onLocationNotFiltered(com.sitewhere.spi.device.event.IDeviceLocation)
     */
    @Override
    public void onLocationNotFiltered(IDeviceLocation location) throws SiteWhereException {
	SolrInputDocument document = SiteWhereSolrFactory.createDocumentFromLocation(location);
	try {
	    queue.put(document);
	} catch (InterruptedException e) {
	    throw new SiteWhereException("Interrupted during indexing.", e);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.device.event.processor.FilteredOutboundEventProcessor#
     * onAlertNotFiltered (com.sitewhere.spi.device.event.IDeviceAlert)
     */
    @Override
    public void onAlertNotFiltered(IDeviceAlert alert) throws SiteWhereException {
	SolrInputDocument document = SiteWhereSolrFactory.createDocumentFromAlert(alert);
	try {
	    queue.put(document);
	} catch (InterruptedException e) {
	    throw new SiteWhereException("Interrupted during indexing.", e);
	}
    }

    /**
     * Class that processes documents in the queue asynchronously.
     * 
     * @author Derek
     */
    private class SolrDocumentQueueProcessor implements Runnable {

	@Override
	public void run() {
	    LOGGER.info("Started Solr indexing thread.");
	    boolean interrupted = false;

	    while (!interrupted) {
		long start = System.currentTimeMillis();
		List<SolrInputDocument> batch = new ArrayList<SolrInputDocument>();

		while (((System.currentTimeMillis() - start) < BATCH_INTERVAL) && (batch.size() <= MAX_BATCH_SIZE)) {
		    try {
			SolrInputDocument document = queue.poll(BATCH_INTERVAL, TimeUnit.MILLISECONDS);
			if (document != null) {
			    batch.add(document);
			}
		    } catch (InterruptedException e) {
			LOGGER.error("Solr indexing thread interrupted.", e);
			interrupted = true;
			break;
		    }
		}
		if (!batch.isEmpty()) {
		    try {
			UpdateResponse response = getSolr().getSolrClient().add(batch, COMMIT_INTERVAL);
			if (response.getStatus() != 0) {
			    LOGGER.warn("Bad response code indexing documents: " + response.getStatus());
			}
			LOGGER.debug("Indexed " + batch.size() + " documents in Solr.");
		    } catch (SolrServerException e) {
			LOGGER.error("Exception indexing SiteWhere document.", e);
		    } catch (IOException e) {
			LOGGER.error("IOException indexing SiteWhere document.", e);
		    } catch (Throwable e) {
			LOGGER.error("Unhandled exception indexing SiteWhere document.", e);
		    }
		}
	    }
	}
    }

    public SiteWhereSolrConfiguration getSolr() {
	return solr;
    }

    public void setSolr(SiteWhereSolrConfiguration solr) {
	this.solr = solr;
    }
}