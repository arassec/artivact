extends Node3D

var exhibitionId: String
var toolData: Variant

var title: String

var items = []
var itemIndex = 0
var maxItemIndex = 0

var loadItem = false
var itemLoaded = false
var itemShown = false
var loadedItem
var shownItem


func _init():
	SignalBus.register(SignalBus.SignalType.NEXT_ITEM, show_next_item)
	SignalBus.register(SignalBus.SignalType.MODEL_LOADED, item_model_loaded)
	

func _ready():
	if items.size() > 0:
		loadItem = true


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.NEXT_ITEM, show_next_item)
	SignalBus.deregister(SignalBus.SignalType.MODEL_LOADED, item_model_loaded)


func _process(_delta: float):
	if loadItem:
		loadItem = false
		load_item()
	elif itemShown && itemLoaded:
		remove_item()
	elif itemLoaded:
		itemLoaded = false
		add_item()


func setup(exhibitionIdInput: String, topicIndexInput: int, toolIndexInput: int):
	exhibitionId = exhibitionIdInput
	toolData = ExhibitionStore.get_tool(exhibitionId, topicIndexInput, toolIndexInput)
	
	if toolData.type == "TITLE":
		title = I18n.translate(toolData.title)
	elif toolData.type == "ITEMS":
		items = toolData.itemIds
		maxItemIndex = items.size() - 1
	

func show_next_item():
	itemIndex = itemIndex + 1
	if itemIndex > maxItemIndex:
		itemIndex = 0
	loadItem = true


func load_item():
	SignalBus.deregister(SignalBus.SignalType.NEXT_ITEM, show_next_item)
	loadedItem = load("res://scenes/exhibition/default_item.tscn").instantiate()
	if loadedItem.setup(exhibitionId, items[itemIndex]):
		itemLoaded = true


func remove_item():
	remove_child(shownItem)
	shownItem.queue_free()
	itemShown = false


func add_item():
	add_child(loadedItem)
	shownItem = loadedItem
	itemShown = true


func item_model_loaded():
	SignalBus.register(SignalBus.SignalType.NEXT_ITEM, show_next_item)
