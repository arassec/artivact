extends Node3D

var exhibitionId: String
var topicIndex: int

var currentTool


func setup(exhibitionIdInput: String, topicIndexInput: int):
	exhibitionId = exhibitionIdInput
	topicIndex = topicIndexInput
	switch_tool(1)  # Load the second tool of the exhibition (for now ignoring titles!)...
		
	
# TODO: Implement switching tools
func switch_tool(toolIndex: int):
	if currentTool:
		remove_child(currentTool)
		currentTool.queue_free()

	currentTool = load("res://scenes/exhibition/default_multi_tool.tscn").instantiate()
	
	currentTool.setup(exhibitionId, topicIndex, toolIndex)
	
	add_child(currentTool)
