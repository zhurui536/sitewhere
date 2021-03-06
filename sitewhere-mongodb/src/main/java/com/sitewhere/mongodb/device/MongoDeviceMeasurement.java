/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.mongodb.device;

import org.bson.Document;

import com.sitewhere.mongodb.MongoConverter;
import com.sitewhere.rest.model.device.event.DeviceMeasurement;
import com.sitewhere.spi.device.event.IDeviceMeasurement;

/**
 * Used to load or save device measurement data to MongoDB.
 * 
 * @author dadams
 */
public class MongoDeviceMeasurement implements MongoConverter<IDeviceMeasurement> {

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.MongoConverter#convert(java.lang.Object)
     */
    @Override
    public Document convert(IDeviceMeasurement source) {
	return MongoDeviceMeasurement.toDocument(source, false);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.sitewhere.mongodb.MongoConverter#convert(org.bson.Document)
     */
    @Override
    public IDeviceMeasurement convert(Document source) {
	return MongoDeviceMeasurement.fromDocument(source, false);
    }

    /**
     * Copy information from SPI into Mongo {@link Document}.
     * 
     * @param source
     * @param target
     * @param isNested
     */
    public static void toDocument(IDeviceMeasurement source, Document target, boolean isNested) {
	MongoDeviceEvent.toDocument(source, target, isNested);

	target.put(MongoDeviceMeasurements.PROP_NAME, source.getName());
	target.put(MongoDeviceMeasurements.PROP_VALUE, source.getValue());
    }

    /**
     * Copy information from Mongo {@link Document} to model object.
     * 
     * @param source
     * @param target
     * @param isNested
     */
    public static void fromDocument(Document source, DeviceMeasurement target, boolean isNested) {
	MongoDeviceEvent.fromDocument(source, target, isNested);

	String name = (String) source.get(MongoDeviceMeasurements.PROP_NAME);
	Double value = (Double) source.get(MongoDeviceMeasurements.PROP_VALUE);
	target.setName(name);
	target.setValue(value);
    }

    /**
     * Convert SPI object to Mongo {@link Document}.
     * 
     * @param source
     * @param isNested
     * @return
     */
    public static Document toDocument(IDeviceMeasurement source, boolean isNested) {
	Document result = new Document();
	MongoDeviceMeasurement.toDocument(source, result, isNested);
	return result;
    }

    /**
     * Convert a {@link Document} into the SPI equivalent.
     * 
     * @param source
     * @param isNested
     * @return
     */
    public static DeviceMeasurement fromDocument(Document source, boolean isNested) {
	DeviceMeasurement result = new DeviceMeasurement();
	MongoDeviceMeasurement.fromDocument(source, result, isNested);
	return result;
    }
}