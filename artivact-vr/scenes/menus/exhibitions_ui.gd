extends Node


# Called when the node enters the scene tree for the first time.
func _ready():
	load_exhibitions()


func load_exhibitions():
	var resourceFiles = DirAccess.get_files_at("res://")
	for resourceFile in resourceFiles:
		if resourceFile.ends_with(".artivact-exhibition.zip"):
			create_exhibition_ui(resourceFile)


func create_exhibition_ui(exhibitionFile: String):
	var exhibitionId = exhibitionFile.replace(".artivact-exhibition.zip", "")
	var topicsContainer = find_child("ExhibitionsContainer")

	var exhibitionButton = Button.new()
	exhibitionButton.text = exhibitionId
	exhibitionButton.pressed.connect(trigger_open_exhibition.bind(exhibitionId))

	var exhibitionButtonContainer = MarginContainer.new()
	exhibitionButtonContainer.add_theme_constant_override("margin_left", 10)
	exhibitionButtonContainer.add_theme_constant_override("margin_right", 10)
	exhibitionButtonContainer.add_theme_constant_override("margin_top", 5)
	exhibitionButtonContainer.add_theme_constant_override("margin_bottom", 5)
	exhibitionButtonContainer.add_child(exhibitionButton)
	
	topicsContainer.add_child(exhibitionButtonContainer);


func trigger_open_exhibition(exhibitionId: String):
	SignalBus.trigger_with_payload(SignalBus.SignalType.OPEN_EXHIBITION, exhibitionId)
