extends Node


# Called when the node enters the scene tree for the first time.
func _ready():
	create_exhibition_ui()


func create_exhibition_ui():
	var topicsContainer = find_child("ExhibitionsContainer")

	# Contains exhibitinoId -> title, e.g. { "123abc": "Demo-Project", ... }
	var availableExhibitions = ExhibitionStore.get_available_exhibitions()
	
	for exhibitionId in availableExhibitions:

		var exhibitionButton = Button.new()
		exhibitionButton.text = availableExhibitions[exhibitionId]
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
