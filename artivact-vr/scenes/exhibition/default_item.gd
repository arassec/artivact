extends Node3D

var exhibitionId: String
var itemId: String

var modelLoaderThread: Thread

var itemData: Variant

var models = []
var modelIndex = 0
var maxModelIndex = 0

var gltfDocument: GLTFDocument
var gltfState: GLTFState
	
var modelLoading = false
var updateModel = false
var glbLoaded = false
var modelLoaded = false
var modelShown = false
var loadedModel: Node
var shownModel: Node

var zoomIn = false
var zoomOut = false


func _init():
	SignalBus.register(SignalBus.SignalType.NEXT_MODEL, next_model)
	SignalBus.register(SignalBus.SignalType.ZOOM_MODEL_IN, zoom_in)
	SignalBus.register(SignalBus.SignalType.ZOOM_MODEL_OUT, zoom_out)
	SignalBus.register(SignalBus.SignalType.ZOOM_MODEL_STOP, zoom_stop)


func _ready():
	if models.size() > 0 && !modelLoading:
		modelLoading = true
		updateModel = true

	SignalBus.trigger_with_multiload(SignalBus.SignalType.UPDATE_ITEM_DATA, exhibitionId, itemData)


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.NEXT_MODEL, next_model)
	SignalBus.deregister(SignalBus.SignalType.ZOOM_MODEL_IN, zoom_in)
	SignalBus.deregister(SignalBus.SignalType.ZOOM_MODEL_OUT, zoom_out)
	SignalBus.deregister(SignalBus.SignalType.ZOOM_MODEL_STOP, zoom_stop)
	if modelLoaderThread.is_started():
		modelLoaderThread.wait_to_finish()
	

func _process(delta: float):
	if updateModel:
		updateModel = false
		if modelLoaderThread.is_started():
			modelLoaderThread.wait_to_finish()
		modelLoaderThread.start(load_model, Thread.PRIORITY_LOW)
	elif glbLoaded:
		glbLoaded = false
		generate_scene()
	elif modelShown && modelLoaded:
		remove_model()
	elif modelLoaded:
		modelLoaded = false
		add_model()
		
	if shownModel:
		var scaleFactor = 10 * delta
		var model = shownModel
		model.rotate(Vector3(0, 1, 0), 0.4 * delta)
		if zoomIn:
			if (model.scale.x < 1.2):
				model.scale = model.scale + Vector3(scaleFactor, scaleFactor, scaleFactor)
				model.position.y = model.position.y - (scaleFactor / 2)
		if zoomOut:
			if (model.scale.x > 0.1):
				model.scale = model.scale - Vector3(scaleFactor, scaleFactor, scaleFactor)
				model.position.y = model.position.y + (scaleFactor / 2)



func setup(exhibitionIdInput: String, itemIdInput: String) -> bool:
	exhibitionId = exhibitionIdInput
	itemId = itemIdInput
	
	modelLoaderThread = Thread.new()

	var zipReader = ExhibitionStore.get_zip_reader(exhibitionId)
	var itemJson := JSON.new()
	var itemJsonString = zipReader.read_file(str(itemId, "/", itemId, ".artivact.json")).get_string_from_utf8()
	var parseResult := itemJson.parse(itemJsonString)
	if parseResult != OK:
		var errMsg = str("default_item.setup(", itemIdInput, "): FAILED - ", parseResult)
		printerr(errMsg)
		return false
		
	itemData = itemJson.data
	
	models = itemData.mediaContent.models
	maxModelIndex = models.size() - 1
	
	return true
	


func next_model():
	if modelLoading:
		return
		
	if models.size() == 1:
		return
		
	modelIndex = modelIndex + 1
	if modelIndex > maxModelIndex:
		modelIndex = 0
	
	modelLoading = true
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
		

func load_model():
	gltfDocument = GLTFDocument.new()
	gltfState = GLTFState.new()

	var zipReader = ExhibitionStore.get_zip_reader(exhibitionId)
	var modelData = zipReader.read_file(str(itemId, "/", models[modelIndex]))
	var result = gltfDocument.append_from_buffer(modelData, "", gltfState, 64)

	if result != OK:
		var errMsg = str("default_item.load_model(", itemId, " / ", models[modelIndex], "): FAILED - ", result)
		printerr(errMsg)
		return

	glbLoaded = true
	

func generate_scene():
	loadedModel = gltfDocument.generate_scene(gltfState)
	
	var size:Vector3
	for n in loadedModel.get_children().size():
		if "MeshInstance3D"	== loadedModel.get_child(n).get_class():
			var mesh:MeshInstance3D = loadedModel.get_child(n)
			size = mesh.get_aabb().size
			break;
	var targetSizeInM = 50 / 100.0 # Size should initially be 50cm.
	var currentSizeInM = size.x
	var scaleFactor = targetSizeInM / currentSizeInM
	loadedModel.scale = Vector3(scaleFactor, scaleFactor, scaleFactor)

	modelLoaded = true


func remove_model():
	if shownModel != null:
		shownModel.visible = false
		shownModel.queue_free()
		modelShown = false


func add_model():
	call_deferred("add_child", loadedModel)
	shownModel = loadedModel
	modelShown = true
	modelLoading = false
	SignalBus.trigger(SignalBus.SignalType.MODEL_LOADED)
