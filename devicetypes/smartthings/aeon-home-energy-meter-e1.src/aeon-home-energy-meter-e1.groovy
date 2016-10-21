/**
 *  Copyright 2015 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Aeon Home Energy Meter
 *
 *  Author: SmartThings
 *
 *  Date: 2013-05-30
 */
metadata {
	definition (name: "Aeon Home Energy Meter E1", namespace: "smartthings", author: "SmartThings") {
		capability "Energy Meter"
		capability "Power Meter"
		capability "Configuration"
		capability "Sensor"
        capability "Battery"
        capability "Refresh"

		command "reset"
        
        attribute "power1", "number"
        attribute "power2", "number"
        attribute "power3", "number"
        attribute "energy1", "number"
        attribute "energy2", "number"
        attribute "energy3", "number"

		fingerprint deviceId: "0x2101", inClusters: " 0x70,0x31,0x72,0x86,0x32,0x80,0x85,0x60"
	}

	// simulator metadata
	simulator {
		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV1.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}
	}

	// tile definitions
	tiles(scale:2) {
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
        valueTile("battery", "device.battery", decoration: "flat", width: 2, height: 2) {
            state "battery", label:'${currentValue}% battery', unit: ""
        }
		valueTile("power1", "device.power1", decoration: "flat", width: 2, height: 2) {
			state "default", label:'(${currentValue} W)'
		}
		valueTile("power2", "device.power2", decoration: "flat", width: 2, height: 2) {
			state "default", label:'(${currentValue} W)'
		}
		valueTile("power3", "device.power3", decoration: "flat", width: 2, height: 2) {
			state "default", label:'(${currentValue} W)'
		}
		valueTile("energy1", "device.energy1", decoration: "flat", width: 2, height: 2) {
			state "default", label:'(${currentValue} kWh)'
		}
		valueTile("energy2", "device.energy2", decoration: "flat", width: 2, height: 2) {
			state "default", label:'(${currentValue} kWh)'
		}
		valueTile("energy3", "device.energy3", decoration: "flat", width: 2, height: 2) {
			state "default", label:'(${currentValue} kWh)'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action: "reset"
		}
		standardTile("refresh", "device.refresh", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
		standardTile("configure", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configuration.configure", icon:"st.secondary.configure"
		}

		main (["power","energy"])
		details(["power", "energy", "battery", "power1", "power2", "power3", "energy1", "energy2", "energy3", "reset", "refresh", "configure"])
	}
    
	preferences {
    	input "voltage", "decimal", title: "Voltage",
              description: "Voltage across each clamp (default 120V)", defaultValue: 120,
              required: false, displayDuringSetup: true
        input "report1", "enum", title: "Report Type 1", multiple: true,
        	options: ["Power (W)", "Power (Clamp 1)", "Power (Clamp 2)", "Power (Clamp 3)", "Energy (kWh)", "Energy (Clamp 1)", "Energy (Clamp 2)", "Energy (Clamp 3)", "Battery"],
            defaultValue: ["Power (W)"]
        input "report2", "enum", title: "Report Type 2", multiple: true,
        	options: ["Power (W)", "Power (Clamp 1)", "Power (Clamp 2)", "Power (Clamp 3)", "Energy (kWh)", "Energy (Clamp 1)", "Energy (Clamp 2)", "Energy (Clamp 3)", "Battery"],
            defaultValue: ["Energy (kWh)"]
        input "report3", "enum", title: "Report Type 3", multiple: true,
        	options: ["Power (W)", "Power (Clamp 1)", "Power (Clamp 2)", "Power (Clamp 3)", "Energy (kWh)", "Energy (Clamp 1)", "Energy (Clamp 2)", "Energy (Clamp 3)", "Battery"],
            defaultValue: ["Battery"]
    	input "report1Interval", "number", title: "Report 1 Interval",
              description: "Seconds (default 720)", defaultValue: 720,
              required: false, displayDuringSetup: true
    	input "report2Interval", "number", title: "Report 2 Interval",
              description: "Seconds (default 720)", defaultValue: 720,
              required: false, displayDuringSetup: true
    	input "report3Interval", "number", title: "Report 3 Interval",
              description: "Seconds (default 720)", defaultValue: 720,
              required: false, displayDuringSetup: true
	}
}

def updated() {
	log.debug "Preferences updated; configuring..."
	response(configure())
}

def parse(String description) {
	def result = null
	def cmd = zwave.parse(description, [0x31: 1, 0x32: 1, 0x60: 3])
	if (cmd) {
		result = createEvent(zwaveEvent(cmd))
	}
	log.debug "Parse returned ${result?.descriptionText} for $description"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd) {
	log.debug "MeterReport: $cmd"
    if (cmd.scale == 0 || cmd.scale == 1) { // energy reading
	    def displayEnergy = false
        def energyUnit = "kWh"
        if (cmd.scale == 1) {
            energyUnit = "kVAh"
        }
        if (cmd.scaledMeterValue != (data.lastDisplayedEnergyMeterValue ?: 0)
                && Math.abs(cmd.scaledMeterValue - (data.lastDisplayedEnergyMeterValue ?: 0)) / cmd.scaledMeterValue > 0.05) {
            data.lastDisplayedEnergyMeterValue = cmd.scaledMeterValue
            displayEnergy = true
        }
	    [name: "energy", value: cmd.scaledMeterValue, unit: energyUnit, displayed: displayEnergy]
    }
	else {
    	log.info "power: ${Math.round(cmd.scaledMeterValue)} W (last displayed: ${data.lastDisplayedPowerMeterValue})"
        def displayPower = false
        if (cmd.scaledMeterValue != (data.lastDisplayedPowerMeterValue ?: 0)
                && Math.abs(cmd.scaledMeterValue - (data.lastDisplayedPowerMeterValue ?: 0)) / cmd.scaledMeterValue > 0.05) {
            data.lastDisplayedPowerMeterValue = cmd.scaledMeterValue
            displayPower = true
        }
		[name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W", displayed: displayPower]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.meterv1.MeterReport cmd, sourceEndPoint) {
	log.debug "MeterReport: $cmd from endpoint $sourceEndPoint"
	if (cmd.scale == 0) {
		[name: "energy$sourceEndPoint", descriptionText: "Energy (Clamp $sourceEndPoint) Is $cmd.scaledMeterValue kWh", value: cmd.scaledMeterValue, unit: "kWh", displayed: false]
	} else if (cmd.scale == 1) {
		[name: "energy$sourceEndPoint", descriptionText: "Energy (Clamp $sourceEndPoint) Is $cmd.scaledMeterValue kVAh", value: cmd.scaledMeterValue, unit: "kVAh", displayed: false]
	}
	else {
    	log.info "power$sourceEndPoint: ${Math.round(cmd.scaledMeterValue)} W"
		[name: "power$sourceEndPoint", descriptionText: "Power (Clamp $sourceEndPoint) Is ${Math.round(cmd.scaledMeterValue)} W", value: Math.round(cmd.scaledMeterValue), unit: "W", displayed: false]
	}
}

def zwaveEvent(physicalgraph.zwave.commands.multichannelv3.MultiChannelCmdEncap cmd) {
        def encapsulatedCommand = cmd.encapsulatedCommand([0x31: 1, 0x32: 1, 0x60: 3])

        // can specify command class versions here like in zwave.parse
        log.debug ("MultiChannelCmdEncap from endpoint ${cmd.sourceEndPoint}: ${encapsulatedCommand}")

        if (encapsulatedCommand) {
                return zwaveEvent(encapsulatedCommand, cmd.sourceEndPoint)
        }
}

def zwaveEvent(physicalgraph.zwave.commands.batteryv1.BatteryReport cmd) {
        def map = [ name: "battery", unit: "%" ]
        if (cmd.batteryLevel == 0xFF) {  // Special value for low battery alert
                map.value = 1
                map.descriptionText = "${device.displayName} has a low battery"
                map.isStateChange = true
        } else {
                map.value = cmd.batteryLevel
        }
        // Store time of last battery update so we don't ask every wakeup, see WakeUpNotification handler
        state.lastbatt = new Date().time
        createEvent(map)
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
    log.trace "Unknown Command: $cmd"
	[:]
}

def refresh() {
	log.info "Refreshing..."
	def cmd = delayBetween([
        zwave.configurationV1.configurationGet(parameterNumber: 1).format(),
        zwave.associationV1.associationGet(groupingIdentifier:1).format(),
		zwave.meterV2.meterGet(scale: 2).format(),
		zwave.meterV2.meterGet(scale: 0).format(),
	], 1000)
    log.debug cmd
    cmd
}

def reset() {
	// No V1 available
    log.info "Resetting Energy (kWh) meter"
	delayBetween([
		zwave.meterV2.meterReset().format(),
		zwave.meterV2.meterGet(scale: 0).format()
	])
}

def configure() {
	def cmd = delayBetween([
    	zwave.associationV1.associationSet(groupingIdentifier:1, nodeId:zwaveHubNodeId).format(),
        zwave.associationV1.associationSet(groupingIdentifier:2, nodeId:zwaveHubNodeId).format(),
        zwave.associationV1.associationSet(groupingIdentifier:3, nodeId:zwaveHubNodeId).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 101, size: 4, scaledConfigurationValue: reportConfigurationValue(settings.report1 ?: "Power (W)")).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 111, size: 4, scaledConfigurationValue: settings.report1Interval ?: 720).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 102, size: 4, scaledConfigurationValue: reportConfigurationValue(settings.report2 ?: "Energy (kWh)")).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 112, size: 4, scaledConfigurationValue: settings.report2Interval ?: 720).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 103, size: 4, scaledConfigurationValue: reportConfigurationValue(settings.report3 ?: "Battery")).format(),
		zwave.configurationV1.configurationSet(parameterNumber: 113, size: 4, scaledConfigurationValue: settings.report3Interval ?: 720).format(),
        zwave.configurationV1.configurationSet(parameterNumber: 1, size: 2, scaledConfigurationValue: settings.voltage ?: 120).format(),
	])
	log.debug cmd
	cmd
}

def reportConfigurationValue(reportTypes) {
	def configurationValue = 0
	(reportTypes ?: '').split(/,/).each {
    	switch (it) {
        	case "Power (W)":
            	configurationValue |= 0x0004
            	break
            case "Power (Clamp 1)":
            	configurationValue |= 0x0100
            	break
            case "Power (Clamp 2)":
            	configurationValue |= 0x0200
            	break
            case "Power (Clamp 3)":
            	configurationValue |= 0x0400
                break
            case "Energy (kWh)":
                configurationValue |= 0x0008
                break
            case "Energy (Clamp 1)":
            	configurationValue |= 0x0800
                break
            case "Energy (Clamp 2)":
            	configurationValue |= 0x1000
                break
            case "Energy (Clamp 3)":
            	configurationValue |= 0x2000
                break
            case "Battery":
            	configurationValue |= 0x0001
                break
        }
    }
    log.debug "Configuration value for $reportTypes is 0b${Integer.toBinaryString(configurationValue)} = 0x${Integer.toHexString(configurationValue)}"
    return configurationValue
}
