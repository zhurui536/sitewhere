/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.web.configuration;

import com.sitewhere.spring.handler.TenantConfigurationParser;
import com.sitewhere.spring.handler.TenantDatastoreParser;
import com.sitewhere.web.configuration.model.AttributeNode;
import com.sitewhere.web.configuration.model.AttributeType;
import com.sitewhere.web.configuration.model.ConfigurationModel;
import com.sitewhere.web.configuration.model.ElementNode;
import com.sitewhere.web.configuration.model.ElementRole;

/**
 * Specifies model used for tenant configuration user interface.
 * 
 * @author Derek
 */
public class TenantConfigurationModel extends ConfigurationModel {

	public TenantConfigurationModel() {
		setLocalName("tenant-configuration");
		setName("Tenant Configuration");
		setDescription("Provides a model for all aspects of tenant configuration.");
		getElements().add(createGlobals());
		getElements().add(createDataManagement());
		getElements().add(createDeviceCommunication());
		getElements().add(createInboundProcessingChain());
		getElements().add(createOutboundProcessingChain());
		getElements().add(createAssetManagement());
	}

	/**
	 * Create the container for global overrides information.
	 * 
	 * @return
	 */
	protected ElementNode createGlobals() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Global Overrides",
						TenantConfigurationParser.Elements.Globals.getLocalName(), "cogs",
						ElementRole.Top_Globals);
		builder.setDescription("Allow tenant-specific changes to global configuration elements.");
		return builder.build();
	}

	/**
	 * Create the container for datastore information.
	 * 
	 * @return
	 */
	protected ElementNode createDataManagement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Data Management",
						TenantConfigurationParser.Elements.TenantDatastore.getLocalName(), "database",
						ElementRole.Top_DataManagement);
		builder.setDescription("Configure the datastore and related aspects such as caching and "
				+ "data model initialization.");
		builder.addElement(createMongoTenantDatastoreElement());
		builder.addElement(createHBaseTenantDatastoreElement());
		builder.addElement(createEHCacheElement());
		builder.addElement(createHazelcastCacheElement());
		builder.addElement(createDefaultDeviceModelInitializerElement());
		builder.addElement(createDefaultAssetModelInitializerElement());
		builder.addElement(createDefaultScheduleModelInitializerElement());
		return builder.build();
	}

	/**
	 * Create the container for device communication information.
	 * 
	 * @return
	 */
	protected ElementNode createDeviceCommunication() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Device Communication",
						TenantConfigurationParser.Elements.DeviceCommunication.getLocalName(), "exchange",
						ElementRole.Top_DeviceCommunication);
		builder.setDescription("Configure how information is received from devices, how data is queued "
				+ "for processing, and how commands are sent to devices.");
		return builder.build();
	}

	/**
	 * Create the container for inbound processing chain configuration.
	 * 
	 * @return
	 */
	protected ElementNode createInboundProcessingChain() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Inbound Processors",
						TenantConfigurationParser.Elements.InboundProcessingChain.getLocalName(), "sign-in",
						ElementRole.Top_InboundProcessingChain);
		builder.setDescription("Configure a chain of processing steps that are applied to inbound data.");
		return builder.build();
	}

	/**
	 * Create the container for outbound processing chain configuration.
	 * 
	 * @return
	 */
	protected ElementNode createOutboundProcessingChain() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Outbound Processors",
						TenantConfigurationParser.Elements.OutboundProcessingChain.getLocalName(),
						"sign-out", ElementRole.Top_OutboundProcessingChain);
		builder.setDescription("Configure a chain of processing steps that are applied to outbound data.");
		return builder.build();
	}

	/**
	 * Create the container for asset management configuration.
	 * 
	 * @return
	 */
	protected ElementNode createAssetManagement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Asset Management",
						TenantConfigurationParser.Elements.AssetManagement.getLocalName(), "tag",
						ElementRole.Top_AssetManagement);
		builder.setDescription("Configure asset management features.");
		return builder.build();
	}

	/**
	 * Create element configuration for MonogoDB tenant datastore.
	 * 
	 * @return
	 */
	protected ElementNode createMongoTenantDatastoreElement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("MongoDB Tenant Datastore",
						TenantDatastoreParser.Elements.MongoTenantDatastore.getLocalName(), "database",
						ElementRole.DataManagement_Datastore);

		builder.setDescription("Store tenant data using a MongoDB database.");
		builder.addAttribute((new AttributeNode.Builder("Use bulk inserts", "useBulkEventInserts",
				AttributeType.Boolean).setDescription("Use the MongoDB bulk insert API to add "
				+ "events in groups and improve performance.").build()));
		builder.addAttribute((new AttributeNode.Builder("Bulk insert max chunk size",
				"bulkInsertMaxChunkSize", AttributeType.Integer).setDescription("Maximum number of records to send "
				+ "in a single bulk insert (if bulk inserts are enabled).").build()));
		return builder.build();
	}

	/**
	 * Create element configuration for HBase tenant datastore.
	 * 
	 * @return
	 */
	protected ElementNode createHBaseTenantDatastoreElement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("HBase Tenant Datastore",
						TenantDatastoreParser.Elements.HBaseTenantDatastore.getLocalName(), "database",
						ElementRole.DataManagement_Datastore);
		builder.setDescription("Store tenant data using tables in an HBase instance.");
		return builder.build();
	}

	/**
	 * Create element configuration for EHCache cache.
	 * 
	 * @return
	 */
	protected ElementNode createEHCacheElement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("EHCache Cache Provider",
						TenantDatastoreParser.Elements.EHCacheDeviceManagementCache.getLocalName(),
						"folder-open-o", ElementRole.DataManagement_CacheProvider);
		return builder.build();
	}

	/**
	 * Create element configuration for Hazelcast cache.
	 * 
	 * @return
	 */
	protected ElementNode createHazelcastCacheElement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Hazelcast Distributed Cache Provider",
						TenantDatastoreParser.Elements.HazelcastCache.getLocalName(), "folder-open-o",
						ElementRole.DataManagement_CacheProvider);
		return builder.build();
	}

	/**
	 * Create element configuration for device model initializer.
	 * 
	 * @return
	 */
	protected ElementNode createDefaultDeviceModelInitializerElement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Device Model Initializer",
						TenantDatastoreParser.Elements.DefaultDeviceModelInitializer.getLocalName(), "flash",
						ElementRole.DataManagement_DeviceModelInitializer);
		return builder.build();
	}

	/**
	 * Create element configuration for device model initializer.
	 * 
	 * @return
	 */
	protected ElementNode createDefaultAssetModelInitializerElement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Asset Model Initializer",
						TenantDatastoreParser.Elements.DefaultAssetModelInitializer.getLocalName(), "flash",
						ElementRole.DataManagement_AssetModelInitializer);
		return builder.build();
	}

	/**
	 * Create element configuration for device model initializer.
	 * 
	 * @return
	 */
	protected ElementNode createDefaultScheduleModelInitializerElement() {
		ElementNode.Builder builder =
				new ElementNode.Builder("Schedule Model Initializer",
						TenantDatastoreParser.Elements.DefaultScheduleModelInitializer.getLocalName(),
						"flash", ElementRole.DataManagement_ScheduleModelInitializer);
		return builder.build();
	}
}