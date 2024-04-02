extends Node3D

var xr_interface: XRInterface

var exhibitionMenu
var	exhibitionScene

func _ready():
	xr_interface = XRServer.find_interface("OpenXR")
	
	if xr_interface and xr_interface.is_initialized():
		print("OpenXR initialized successfully")
		# Turn off v-sync!
		DisplayServer.window_set_vsync_mode(DisplayServer.VSYNC_DISABLED)
		# Change our main viewport to output to the HMD
		get_viewport().use_xr = true
		# TODO: get_viewport().msaa_3d = Viewport.MSAA_4X not working?!?
	else:
		print("OpenXR not initialized, please check if your headset is connected")
	
	exhibitionMenu = load("res://scenes/menus/exhibitions.tscn").instantiate()
	add_child(exhibitionMenu)
	
	SignalBus.register(SignalBus.SignalType.OPEN_EXHIBITION, open_exhibition)
	SignalBus.register(SignalBus.SignalType.CLOSE_EXHIBITION, close_exhibition)
	
	#open_exhibition("7a2b5479-1813-4a29-88a1-78010ff20970")


func _on_right_controller_input_vector_2_changed(eventName, value):
	if eventName == "primary" and value.x == 1:
		SignalBus.trigger(SignalBus.SignalType.NEXT_ITEM)
	elif eventName == "primary" and value.y == 1:
		SignalBus.trigger(SignalBus.SignalType.ZOOM_MODEL_IN)
	elif eventName == "primary" and value.y == -1:
		SignalBus.trigger(SignalBus.SignalType.ZOOM_MODEL_OUT)
	elif eventName == "primary" and value.y != 1 and value.y != -1:
		SignalBus.trigger(SignalBus.SignalType.ZOOM_MODEL_STOP)


func _input(event):
	if event is InputEventKey && !event.pressed && event.keycode == Key.KEY_RIGHT:
		SignalBus.trigger(SignalBus.SignalType.NEXT_ITEM)
	elif event is InputEventMouseButton && event.button_index == 4:
		SignalBus.trigger(SignalBus.SignalType.ZOOM_MODEL_IN)
	elif event is InputEventMouseButton && event.button_index == 5:
		SignalBus.trigger(SignalBus.SignalType.ZOOM_MODEL_OUT)
	elif event is InputEventKey && !event.pressed && event.keycode == Key.KEY_D:
		SignalBus.trigger_with_payload(SignalBus.SignalType.OPEN_EXHIBITION, "420cc38a-cd9e-4d82-a462-17e2ee55a4fb")
	elif event is InputEventKey && !event.pressed && event.keycode == Key.KEY_M:
		SignalBus.trigger_with_payload(SignalBus.SignalType.OPEN_EXHIBITION, "7a2b5479-1813-4a29-88a1-78010ff20970")
	elif event is InputEventKey && !event.pressed && event.keycode == Key.KEY_UP:
		SignalBus.trigger_with_payload(SignalBus.SignalType.SWITCH_TOPIC, 0)	
	elif event is InputEventKey && !event.pressed && event.keycode == Key.KEY_DOWN:
		SignalBus.trigger_with_payload(SignalBus.SignalType.SWITCH_TOPIC, 1)	
	elif event is InputEventKey && !event.pressed && event.keycode == Key.KEY_Q:
		SignalBus.trigger(SignalBus.SignalType.CLOSE_EXHIBITION)	


func open_exhibition(exhibitionId: String):
	remove_child(exhibitionMenu)
	exhibitionScene = load("res://scenes/exhibition/default_exhibition.tscn").instantiate()
	exhibitionScene.setup(exhibitionId)
	add_child(exhibitionScene)


func close_exhibition():
	if exhibitionScene:
		remove_child(exhibitionScene)
		exhibitionScene.queue_free()
		exhibitionScene = null
	add_child(exhibitionMenu)	
