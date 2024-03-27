extends Node3D

var zipReader:ZIPReader
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


func setup(zipReaderInput: ZIPReader, toolDataInput: Variant):
	zipReader = zipReaderInput
	toolData = toolDataInput
		
	if toolData.type == "TITLE":
		title = toolData.title.value
	elif toolData.type == "ITEMS":
		items = toolData.itemIds
		maxItemIndex = items.size() - 1

	SignalBus.register(SignalBus.SignalType.NEXT_ITEM, next_item)
	

func next_item():
	itemIndex = itemIndex + 1
	if itemIndex > maxItemIndex:
		itemIndex = 0
	loadItem = true
	

func _enter_tree():
	if items.size() > 0:
		loadItem = true


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.NEXT_ITEM, next_item)


func _process(_delta: float):
	if loadItem:
		loadItem = false
		_load_item()
	elif itemShown && itemLoaded:
		_remove_item()
	elif itemLoaded:
		itemLoaded = false
		_add_item()


func _load_item():
	loadedItem = load("res://scenes/exhibition/default_item.tscn").instantiate()
	loadedItem.setup(zipReader, items[itemIndex])
	itemLoaded = true


func _remove_item():
	shownItem.visible = false
	shownItem.queue_free()
	itemShown = false


func _add_item():
	add_child(loadedItem)
	shownItem = loadedItem
	itemShown = true
