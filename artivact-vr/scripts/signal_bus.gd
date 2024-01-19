extends Node

enum SignalType { 
	DEBUG, 
	ERROR,
	NEXT_TOPIC,
	PREVIOUS_TOPIC,
	NEXT_TOOL,
	PREVIOUS_TOOL,
	UPDATE_ITEM_DATA,
	NEXT_ITEM,
	NEXT_MODEL,
	ZOOM_MODEL_IN, 
	ZOOM_MODEL_OUT, 
	ZOOM_MODEL_STOP
}

var callbacks = {
	SignalType.DEBUG: [],
	SignalType.ERROR: [],
	SignalType.NEXT_TOPIC: [],
	SignalType.PREVIOUS_TOPIC: [],
	SignalType.NEXT_TOOL: [],
	SignalType.PREVIOUS_TOOL: [],
	SignalType.UPDATE_ITEM_DATA: [],
	SignalType.NEXT_ITEM: [],
	SignalType.NEXT_MODEL: [],
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


func debug(payload: String):
	for callback in callbacks[SignalType.DEBUG]:
		callback.call(payload)


func error(payload: String):
	for callback in callbacks[SignalType.ERROR]:
		callback.call(payload)
