extends Node3D

var zipReader: ZIPReader
var topicData: Variant


func setup(zipReaderInput: ZIPReader, topicDataInput: Variant):
	zipReader = zipReaderInput
	topicData = topicDataInput


func _enter_tree():
	var currentTool = load("res://scenes/exhibition/default_multi_tool.tscn").instantiate()
	# TODO: Implement switching tools!
	currentTool.setup(zipReader, topicData.tools[1])
	
	add_child(currentTool)
