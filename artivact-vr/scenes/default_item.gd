extends Node3D

var zipReader:ZIPReader
var itemId: String

var modelLoaderThread: Thread

var itemData: Variant

var models = []
var modelIndex = 0
var maxModelIndex = 0

var gltfDocument: GLTFDocument
var gltfState: GLTFState
	
var updateModel = false
var glbLoaded = false
var modelLoaded = false
var modelShown = false
var loadedModel: Node
var shownModel: Node

var zoomIn = false
var zoomOut = false


func setup(zipReaderInput: ZIPReader, itemIdInput: String):
	zipReader = zipReaderInput
	itemId = itemIdInput
	
	modelLoaderThread = Thread.new()

	var itemJson := JSON.new()
	var itemJsonString = zipReader.read_file(str(itemId, "/", itemId, ".artivact.json")).get_string_from_utf8()
	var parseResult := itemJson.parse(itemJsonString)
	if parseResult != OK:
		var errMsg = str("default_item.setup(", itemIdInput, "): FAILED - ", parseResult)
		SignalBus.debug(errMsg)
		printerr(errMsg)
		return
		
	itemData = itemJson.data
	
	models = itemData.mediaContent.models
	maxModelIndex = models.size() - 1
	
	SignalBus.register(SignalBus.SignalType.NEXT_MODEL, next_model)
	SignalBus.register(SignalBus.SignalType.ZOOM_MODEL_IN, zoom_in)
	SignalBus.register(SignalBus.SignalType.ZOOM_MODEL_OUT, zoom_out)
	SignalBus.register(SignalBus.SignalType.ZOOM_MODEL_STOP, zoom_stop)

func next_model():
	SignalBus.trigger_with_payload(SignalBus.SignalType.DEBUG, "next_model()")
	
	if models.size() == 1:
		return
		
	modelIndex = modelIndex + 1
	if modelIndex > maxModelIndex:
		modelIndex = 0
	updateModel = true
	

func zoom_in():
	if shownModel != null:
		zoomIn = true


func zoom_out():
	if shownModel != null:
		zoomOut = true


func zoom_stop():
		zoomIn = false
		zoomOut = false


func _enter_tree():
	if itemData.title.has("value"):
		SignalBus.debug(itemData.title.value)
	if models.size() > 0:
		updateModel = true


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.NEXT_MODEL, next_model)
	SignalBus.deregister(SignalBus.SignalType.ZOOM_MODEL_IN, zoom_in)
	SignalBus.deregister(SignalBus.SignalType.ZOOM_MODEL_OUT, zoom_out)
	SignalBus.deregister(SignalBus.SignalType.ZOOM_MODEL_STOP, zoom_stop)
	if modelLoaderThread.is_started():
		modelLoaderThread.wait_to_finish()
	

func _process(delta: float):
	rotate(Vector3(0, 1, 0), 0.5 * delta)
	
	if updateModel:
		updateModel = false
		if modelLoaderThread.is_started():
			modelLoaderThread.wait_to_finish()
		modelLoaderThread.start(_load_model, Thread.PRIORITY_LOW)
	elif glbLoaded:
		glbLoaded = false
		_generate_scene()
	elif modelShown && modelLoaded:
		_remove_model()
	elif modelLoaded:
		modelLoaded = false
		_add_model()
		
	var scaleFactor = 10 * delta
	if zoomIn:
		if (shownModel.scale.x < 2):
			shownModel.scale = shownModel.scale + Vector3(scaleFactor, scaleFactor, scaleFactor)
			shownModel.position.y = shownModel.position.y - (scaleFactor / 2)
	if zoomOut:
		if (shownModel.scale.x > 0.1):
			shownModel.scale = shownModel.scale - Vector3(scaleFactor, scaleFactor, scaleFactor)
			shownModel.position.y = shownModel.position.y + (scaleFactor / 2)


func _load_model():
	gltfDocument = GLTFDocument.new()
	gltfState = GLTFState.new()

	var modelData := zipReader.read_file(str(itemId, "/", models[modelIndex]))
	var result = gltfDocument.append_from_buffer(modelData, "", gltfState, 64)

	if result != OK:
		var errMsg = str("item.load_model(", models[modelIndex], "): FAILED - ", result)
		SignalBus.debug(errMsg)
		printerr(errMsg)
		return

	glbLoaded = true
	

func _generate_scene():
	loadedModel = gltfDocument.generate_scene(gltfState)
	
	var size:Vector3
	for n in loadedModel.get_children().size():
		var mesh:MeshInstance3D = loadedModel.get_child(n)
		size = mesh.get_aabb().size
		break;
	var targetSizeInM = 50 / 100.0 # Size should initially be 50cm.
	var currentSizeInM = size.x
	var scaleFactor = targetSizeInM / currentSizeInM
	loadedModel.scale = Vector3(scaleFactor, scaleFactor, scaleFactor)

	modelLoaded = true


func _remove_model():
	if shownModel != null:
		shownModel.visible = false
		shownModel.queue_free()
		modelShown = false


func _add_model():
	call_deferred("add_child", loadedModel)
	shownModel = loadedModel
	modelShown = true
