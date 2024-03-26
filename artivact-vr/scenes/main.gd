extends Node3D

var xr_interface: XRInterface

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
	
	var	exhibitionScene = load("res://scenes/default_exhibition.tscn").instantiate()
	# TODO: Load scene dynamically when main menu is implemented!
	exhibitionScene.setup("420cc38a-cd9e-4d82-a462-17e2ee55a4fb")
	add_child(exhibitionScene)
	
	SignalBus.register(SignalBus.SignalType.DEBUG, update_debug_panel)


func update_debug_panel(message: String):
	if $DebugPanel != null:
		$DebugPanel.text = message
	

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
