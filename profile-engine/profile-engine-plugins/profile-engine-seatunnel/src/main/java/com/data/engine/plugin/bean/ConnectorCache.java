package com.data.engine.plugin.bean;

import com.data.engine.plugin.utils.PluginDiscoveryUtil;
import com.data.profile.common.exception.ProfileException;
import lombok.NonNull;
import org.apache.seatunnel.api.configuration.util.OptionRule;
import org.apache.seatunnel.common.config.Common;
import org.apache.seatunnel.common.config.DeployMode;
import org.apache.seatunnel.common.constants.PluginType;
import org.apache.seatunnel.plugin.discovery.PluginIdentifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ConnectorCache {
    private final ConcurrentMap<PluginIdentifier, OptionRule> allConnectorOptionRule = new ConcurrentHashMap<>();

    // private final ConcurrentMap<PluginType, List<ConnectorInfo>> allConnectorCache = new ConcurrentHashMap<>();
    // private final ConcurrentMap<PluginType, List<ConnectorInfo>> downloadConnectorCache = new ConcurrentHashMap<>();

    /*private List<ConnectorInfo> transformCache = new CopyOnWriteArrayList<>();
    private ConcurrentMap<String, FormStructure> sourceFormStructureCache = new ConcurrentHashMap<>();
    private ConcurrentMap<String, FormStructure> sinkFormStructureCache = new ConcurrentHashMap<>();
    private ConcurrentMap<String, FormStructure> transformFormStructureCache = new ConcurrentHashMap<>();
    private Map<PluginIdentifier, ConnectorFeature> featureMap = new HashMap<>();*/

    public ConnectorCache() throws IOException {
        refresh();
    }

    public synchronized void refresh() throws IOException {
        Common.setDeployMode(DeployMode.CLIENT);
        Map<PluginType, LinkedHashMap<PluginIdentifier, OptionRule>> allConnectors = PluginDiscoveryUtil.getAllConnectors();

        // Connector OptionRule
        allConnectorOptionRule.clear();
        allConnectors.forEach((key, value) -> allConnectorOptionRule.putAll(value));

        /*downloadConnectorCache.put(PluginType.SOURCE, PluginDiscoveryUtil.getDownloadedConnectors(allConnectors, PluginType.SOURCE));
        downloadConnectorCache.put(PluginType.SINK, PluginDiscoveryUtil.getDownloadedConnectors(allConnectors, PluginType.SINK));

        allConnectorCache.put(PluginType.SOURCE, PluginDiscoveryUtil.getAllConnectorsFromPluginMapping(PluginType.SOURCE));
        allConnectorCache.put(PluginType.SINK, PluginDiscoveryUtil.getAllConnectorsFromPluginMapping(PluginType.SINK));

        transformCache = PluginDiscoveryUtil.getTransforms(allConnectors);
        sourceFormStructureCache = PluginDiscoveryUtil.getDownloadedConnectorFormStructures(allConnectors, PluginType.SOURCE);
        sinkFormStructureCache = PluginDiscoveryUtil.getDownloadedConnectorFormStructures(allConnectors, PluginType.SINK);
        transformFormStructureCache = PluginDiscoveryUtil.getTransformFormStructures(allConnectors);
        syncSourceFeature();*/
    }

    public OptionRule getOptionRule(@NonNull String pluginType, @NonNull String connectorName) {
        return allConnectorOptionRule.get(PluginIdentifier.of("seatunnel", pluginType, connectorName));
    }

    /*public List<ConnectorInfo> getAllConnectors(PluginType pluginType) {
        return allConnectorCache.get(pluginType);
    }

    public List<ConnectorInfo> getTransform() {
        return transformCache;
    }

    public List<ConnectorInfo> getDownLoadConnector(PluginType pluginType) {
        return downloadConnectorCache.get(pluginType);
    }

    public List<ConnectorInfo> getNotDownLoadConnector(PluginType pluginType) {
        Map<PluginIdentifier, ConnectorInfo> allConnectors =
                allConnectorCache.get(pluginType).stream()
                        .collect(
                                Collectors.toMap(
                                        ConnectorInfo::getPluginIdentifier, Function.identity()));
        downloadConnectorCache
                .get(pluginType)
                .forEach(d -> allConnectors.remove(d.getPluginIdentifier()));
        return new ArrayList<>(allConnectors.values());
    }

    public ConnectorFeature getConnectorFeature(PluginIdentifier connectorInfo) {
        return featureMap.get(connectorInfo);
    }*/



    /*private void syncSourceFeature() throws IOException {
        featureMap = PluginDiscoveryUtil.getConnectorFeatures(PluginType.SOURCE);
    }

    public FormStructure getFormStructure(@NonNull String pluginType, @NonNull String connectorName) {
        if (PluginType.SOURCE.getType().equals(pluginType)) {
            return sourceFormStructureCache.get(connectorName);
        }

        if (PluginType.TRANSFORM.getType().equals(pluginType)) {
            return transformFormStructureCache.get(connectorName);
        }

        if (PluginType.SINK.getType().equals(pluginType)) {
            return sinkFormStructureCache.get(connectorName);
        }

        throw new ProfileException(ProfileErrorEnum.UNSUPPORTED_CONNECTOR_TYPE, pluginType);
    }*/
}
