extends Node

enum SignalType { 
	ERROR,
	OPEN_EXHIBITION,
	CLOSE_EXHIBITION,
	UPDATE_EXHIBITION_NAVIGATION,
	SWITCH_TOPIC,
	NEXT_TOOL,
	PREVIOUS_TOOL,
	UPDATE_ITEM_DATA,
	NEXT_ITEM,
	NEXT_MODEL,
	MODEL_LOADED,
	ZOOM_MODEL_IN, 
	ZOOM_MODEL_OUT, 
	ZOOM_MODEL_STOP
}

var callbacks = {
	SignalType.ERROR: [],
	SignalType.OPEN_EXHIBITION: [],
	SignalType.CLOSE_EXHIBITION: [],
	SignalType.UPDATE_EXHIBITION_NAVIGATION: [],
	SignalType.SWITCH_TOPIC: [],
	SignalType.NEXT_TOOL: [],
	SignalType.PREVIOUS_TOOL: [],
	SignalType.UPDATE_ITEM_DATA: [],
	SignalType.NEXT_ITEM: [],
	SignalType.NEXT_MODEL: [],
	SignalType.MODEL_LOADED: [],
	SignalType.ZOOM_MODEL_IN: [],
	SignalType.ZOOM_MODEL_OUT: [],
	SignalType.ZOOM_MODEL_STOP: [],
}


func register(type: SignalType, callback: Callable):
	callbacks[type].push_back(callback)


func deregister(type: SignalType, callback: Callable):
	callbacks[type].erase(callback)


func trigger(type: SignalType):
	for callback in callbacks[type]:
		callback.call()


func trigger_with_payload(type: SignalType, payload: Variant):
	for callback in callbacks[type]:
		callback.call(payload)


func trigger_with_multiload(type: SignalType, payloadOne: Variant, payloadTwo: Variant):
	for callback in callbacks[type]:
		callback.call(payloadOne, payloadTwo)


func error(payload: String):
	for callback in callbacks[SignalType.ERROR]:
		callback.call(payload)
