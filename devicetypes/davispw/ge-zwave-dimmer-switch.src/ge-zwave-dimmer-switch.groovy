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
 */
metadata {
	definition (name: "GE Z-Wave Dimmer Switch", namespace: "davispw", author: "Peter Davis") {
		capability "Switch Level"
		capability "Actuator"
		capability "Indicator"
		capability "Switch"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
        
		attribute "ignoreStartLevel", "string"
        attribute "invertSwitch", "string"
        attribute "onOffFadeRate", "number"
        attribute "onOffFadeSteps", "number"
        attribute "onOffFadeDurationDisplay", "string"
        attribute "dimFadeRate", "number"
        attribute "dimFadeSteps", "number"
        attribute "dimFadeDurationDisplay", "string"
        attribute "manualFadeRate", "number"
        attribute "manualFadeSteps", "number"
        attribute "manualFadeDurationDisplay", "string"

        command "setOnOffFadeRate"
        command "setOnOffFadeSteps"
        command "setManualFadeRate"
        command "setManualFadeSteps"
        command "setDimFadeRate"
        command "setDimFadeSteps"
        command "invertSwitch"
        command "toggleInvertSwitch"
        command "ignoreStartLevel"
        command "toggleIgnoreStartLevel"

		fingerprint inClusters: "0x26"
	}

	simulator {
		status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		status "09%": "command: 2003, payload: 09"
		status "10%": "command: 2003, payload: 0A"
		status "33%": "command: 2003, payload: 21"
		status "66%": "command: 2003, payload: 42"
		status "99%": "command: 2003, payload: 63"

		// reply messages
		reply "2001FF,delay 5000,2602": "command: 2603, payload: FF"
		reply "200100,delay 5000,2602": "command: 2603, payload: 00"
		reply "200119,delay 5000,2602": "command: 2603, payload: 19"
		reply "200132,delay 5000,2602": "command: 2603, payload: 32"
		reply "20014B,delay 5000,2602": "command: 2603, payload: 4B"
		reply "200163,delay 5000,2602": "command: 2603, payload: 63"
	}

	tiles(scale: 2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true) {
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
				attributeState "level", action:"switch level.setLevel"
			}
		}

		standardTile("indicator", "device.indicatorStatus", width: 2, height: 2, decoration: "flat") {
			state "when off", action:"indicator.indicatorWhenOn", icon:"st.indicators.lit-when-off"
			state "when on", action:"indicator.indicatorNever", icon:"st.indicators.lit-when-on"
			state "never", action:"indicator.indicatorWhenOff", icon:"st.indicators.never-lit"
		}

		standardTile("refresh", "device.switch", width: 2, height: 2, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		valueTile("level", "device.level", decoration: "flat", width: 2, height: 2) {
			state "level", label:'${currentValue} %', unit:"%", backgroundColor:"#ffffff"
		}
        
		controlTile("onOffFadeRate", "device.onOffFadeRate", "slider", width: 4, height: 1, range: '(1..255)') {
			state "default", action:"setOnOffFadeRate"
		}

		controlTile("onOffFadeSteps", "device.onOffFadeSteps", "slider", width: 4, height: 1, range: '(1..99)') {
			state "default", action:"setOnOffFadeSteps"
		}
        
		valueTile("onOffFade", "device.onOffFadeDurationDisplay", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} on/off'
		}
        
		controlTile("manualFadeRate", "device.manualFadeRate", "slider", width: 4, height: 1, range: '(1..255)') {
			state "default", action:"setManualFadeRate"
		}

		controlTile("manualFadeSteps", "device.manualFadeSteps", "slider", width: 4, height: 1, range: '(1..99)') {
			state "default", action:"setManualFadeSteps"
		}
        
		valueTile("manualFade", "device.manualFadeDurationDisplay", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} manual dim'
		}
  
  		controlTile("dimFadeRate", "device.dimFadeRate", "slider", width: 4, height: 1, range: '(1..255)') {
			state "default", action:"setDimFadeRate"
		}

		controlTile("dimFadeSteps", "device.dimFadeSteps", "slider", width: 4, height: 1, range: '(1..99)') {
			state "default", action:"setDimFadeSteps"
		}
        
		valueTile("dimFade", "device.dimFadeDurationDisplay", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} dim'
		}
        
		valueTile("invert", "device.invertSwitch", width: 3, height: 1, decoration: "flat") {
			state "default", label: 'switch ${currentValue}', action:"toggleInvertSwitch"
		}
        
		valueTile("ignoreStartLevel", "device.ignoreStartLevel", width: 3, height: 1, decoration: "flat") {
			state "default", label: '${currentValue} start level', action:"toggleIgnoreStartLevel"
		}

		main(["switch"])
		details(["switch", "level", "indicator", "refresh",
        	"onOffFadeRateLabel", "onOffFadeRate", "onOffFadeSteps", "onOffFade",
            "manualFadeRate", "manualFadeSteps", "manualFade",
            "dimFadeRate", "dimFadeSteps", "dimFade",
            "invert", "ignoreStartLevel"])
	}
}

def parse(String description) {
	def result = null
	if (description != "updated") {
		log.debug "parse() >> zwave.parse($description)"
		def cmd = zwave.parse(description, [0x20: 1, 0x26: 1, 0x70: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
	}
	if (result?.name == 'hail' && hubFirmwareLessThan("000.011.00602")) {
		result = [result, response(zwave.basicV1.basicGet())]
		log.debug "Was hailed: requesting state update"
	} else {
		log.debug "Parse returned ${result?.descriptionText}"
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelReport cmd) {
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelSet cmd) {
	dimmerEvents(cmd)
}

private dimmerEvents(physicalgraph.zwave.Command cmd) {
	def value = (cmd.value ? "on" : "off")
	def result = [createEvent(name: "switch", value: value)]
	if (cmd.value && cmd.value <= 100) {
		result << createEvent(name: "level", value: cmd.value, unit: "%")
	}
	return result
}

def synchronized zwaveEvent(physicalgraph.zwave.commands.configurationv1.ConfigurationReport cmd) {
	log.debug "ConfigurationReport $cmd"
    def value = cmd.configurationValue[0]
    	
	switch (cmd.parameterNumber) {
    case 3: return createEvent([name: "indicatorStatus", value:
        	value == 1 ? "when on" : value == 2 ? "never" : "when off"])
    case 4: return createEvent(name: "invertSwitch", value: value == 1 ? "inverted" : "normal")
    case 5: return createEvent(name: "ignoreStartLevel", value: value == 1 ? "ignoring" : "using")
    
    case 12: return [createEvent(name: "onOffFadeRate", value: value),
        	createEvent(name: "onOffFadeDurationDisplay", value:
        		String.format("%.2f seconds", getFadeDuration(
                	device.currentValue("onOffFadeRate"),
                    device.currentValue("onOffFadeSteps"))),
                displayed: false)]
    case 11: return [createEvent(name: "onOffFadeSteps", value: value),
        	createEvent(name: "onOffFadeDurationDisplay", value:
        		String.format("%.2f seconds", getFadeDuration(
                	device.currentValue("onOffFadeRate"),
                    device.currentValue("onOffFadeSteps"))),
                displayed: false)]   
                
    case 8: return [createEvent(name: "manualFadeRate", value: value),
        	createEvent(name: "manualFadeDurationDisplay", value:
        		String.format("%.2f seconds", getFadeDuration(
                	device.currentValue("manualFadeRate"),
                    device.currentValue("manualFadeSteps"))),
                displayed: false)]
    case 7: return [createEvent(name: "manualFadeSteps", value: value),
        	createEvent(name: "manualFadeDurationDisplay", value:
        		String.format("%.2f seconds", getFadeDuration(
                	device.currentValue("manualFadeRate"),
                    device.currentValue("manualFadeSteps"))),
                displayed: false)]   

    case 10: return [createEvent(name: "dimFadeRate", value: value),
        	createEvent(name: "dimFadeDurationDisplay", value:
        		String.format("%.2f seconds", getFadeDuration(
                	device.currentValue("dimFadeRate"),
                    device.currentValue("dimFadeSteps"))),
                displayed: false)]
    case 9: return [createEvent(name: "dimFadeSteps", value: value),
        	createEvent(name: "dimFadeDurationDisplay", value:
        		String.format("%.2f seconds", getFadeDuration(
                	device.currentValue("dimFadeRate"),
                    device.currentValue("dimFadeSteps"))),
                displayed: false)]   
	}
}

def zwaveEvent(physicalgraph.zwave.commands.hailv1.Hail cmd) {
	createEvent([name: "hail", value: "hail", descriptionText: "Switch button was pressed", displayed: false])
}

def zwaveEvent(physicalgraph.zwave.commands.manufacturerspecificv2.ManufacturerSpecificReport cmd) {
	log.debug "manufacturerId:   ${cmd.manufacturerId}"
	log.debug "manufacturerName: ${cmd.manufacturerName}"
	log.debug "productId:        ${cmd.productId}"
	log.debug "productTypeId:    ${cmd.productTypeId}"
	def msr = String.format("%04X-%04X-%04X", cmd.manufacturerId, cmd.productTypeId, cmd.productId)
	updateDataValue("MSR", msr)
	createEvent([descriptionText: "$device.displayName MSR: $msr", isStateChange: false])
}

def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv1.SwitchMultilevelStopLevelChange cmd) {
	[createEvent(name:"switch", value:"on"), response(zwave.switchMultilevelV1.switchMultilevelGet().format())]
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	// Handles all Z-Wave commands we aren't interested in
	[:]
}

def on() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0xFF).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def off() {
	delayBetween([
			zwave.basicV1.basicSet(value: 0x00).format(),
			zwave.switchMultilevelV1.switchMultilevelGet().format()
	],5000)
}

def setLevel(value) {
	log.debug "setLevel >> value: $value"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	if (level > 0) {
		sendEvent(name: "switch", value: "on")
	} else {
		sendEvent(name: "switch", value: "off")
	}
	sendEvent(name: "level", value: level, unit: "%")
	delayBetween ([zwave.basicV1.basicSet(value: level).format(), zwave.switchMultilevelV1.switchMultilevelGet().format()], 5000)
}

def setLevel(value, duration) {
	log.debug "setLevel >> value: $value, duration: $duration"
	def valueaux = value as Integer
	def level = Math.max(Math.min(valueaux, 99), 0)
	def dimmingDuration = duration < 128 ? duration : 128 + Math.round(duration / 60)
	def getStatusDelay = duration < 128 ? (duration*1000)+2000 : (Math.round(duration / 60)*60*1000)+2000
	delayBetween ([zwave.switchMultilevelV2.switchMultilevelSet(value: level, dimmingDuration: dimmingDuration).format(),
				   zwave.switchMultilevelV1.switchMultilevelGet().format()], getStatusDelay)
}

def poll() {
	zwave.switchMultilevelV1.switchMultilevelGet().format()
}

def refresh() {
	log.debug "refresh() is called"
	def commands = []
	commands << zwave.switchMultilevelV1.switchMultilevelGet().format()
	if (getDataValue("MSR") == null) {
		commands << zwave.manufacturerSpecificV1.manufacturerSpecificGet().format()
	}
	commands << zwave.configurationV1.configurationGet(parameterNumber: 11).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 7).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 9).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 3).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 4).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 5).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 12).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 8).format()
	commands << zwave.configurationV1.configurationGet(parameterNumber: 10).format()
	delayBetween(commands, 1000)
}

def indicatorWhenOn() {
	log.debug "indicatorWhenOn()"
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 3, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 3).format()])
}

def indicatorWhenOff() {
	log.debug "indicatorWhenOff()"
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 3, size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 3).format()])
}

def indicatorNever() {
	log.debug "indicatorNever()"
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [2], parameterNumber: 3, size: 1).format(),
    	zwave.configurationV1.configurationGet(parameterNumber: 3).format()])
}

def toggleInvertSwitch() {
	invertSwitch(device.currentValue("invertSwitch") == "inverted" ? false : true)
}

def invertSwitch(invert=true) {
	log.debug "invertSwitch(invert=$invert)"
	if (invert) {
		delayBetween([
        	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 4, size: 1).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 4).format()])
	}
	else {
		delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 4, size: 1).format(),
            zwave.configurationV1.configurationGet(parameterNumber: 4).format()])
	}
}

def toggleIgnoreStartLevel() {
	ignoreStartLevel(device.currentValue("ignoreStartLevel") == "using" ? true : false)
}

def ignoreStartLevel(ignore=true) {
	log.debug "ignoreStartLevel(ignore=$ignore)"
	if (ignore) {
		delayBetween([
        	zwave.configurationV1.configurationSet(configurationValue: [1], parameterNumber: 5, size: 1).format(),
		    zwave.configurationV1.configurationGet(parameterNumber: 5).format()])
	}
	else {
		delayBetween([
			zwave.configurationV1.configurationSet(configurationValue: [0], parameterNumber: 5, size: 1).format(),
		    zwave.configurationV1.configurationGet(parameterNumber: 5).format()])
	}
}

def setOnOffFadeRate(rate) {
	log.debug "setOnOffFadeRate(rate=$rate)"
	if (rate < 1) rate = 1
    if (rate > 99) rate = 255
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [rate], parameterNumber: 12, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 12).format()
    ])
}

def setOnOffFadeSteps(steps) {
	log.debug "setOnOffFadeSteps(steps=$steps)"
	if (steps < 1) steps = 1
    if (steps > 99) steps = 99
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [steps], parameterNumber: 11, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 11).format()
    ])
}

def setManualFadeRate(rate) {
	log.debug "setManualFadeRate(rate=$rate)"
	if (rate < 1) rate = 1
    if (rate > 99) rate = 255
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [rate], parameterNumber: 8, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 8).format()
    ])
}

def setManualFadeSteps(steps) {
	log.debug "setManualFadeSteps(steps=$steps)"
	if (steps < 1) steps = 1
    if (steps > 99) steps = 99
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [steps], parameterNumber: 7, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 7).format()
    ])
}

def setDimFadeRate(rate) {
	log.debug "setDimFadeRate(rate=$rate)"
	if (rate < 1) rate = 1
    if (rate > 99) rate = 255
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [rate], parameterNumber: 10, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 10).format()
    ])
}

def setDimFadeSteps(steps) {
	log.debug "setDimFadeSteps(steps=$steps)"
	if (steps < 1) steps = 1
    if (steps > 99) steps = 99
	delayBetween([
    	zwave.configurationV1.configurationSet(configurationValue: [steps], parameterNumber: 9, size: 1).format(),
		zwave.configurationV1.configurationGet(parameterNumber: 9).format()
    ])
}

def getFadeDuration(rate, steps) {
	if (rate == 1 && steps == 99) {
    	return 0.0 // show as "instant"
    } else if (rate != null && steps != null) {
        def minDim = 1.0
        def availableSteps = 99 - minDim + 1
        return ((availableSteps / steps) * rate / 100.0).setScale(2, BigDecimal.ROUND_HALF_UP)
    }
}
