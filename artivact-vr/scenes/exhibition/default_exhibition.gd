extends Node3D

var exhibitionId: String

var currentTopic


func _init():
	SignalBus.register(SignalBus.SignalType.SWITCH_TOPIC, switch_topic)


func _ready():
	if $ExhibitionTitleLabel != null:
		$ExhibitionTitleLabel.text = ExhibitionStore.get_exhibition_title(exhibitionId)

	if $ExhibitionDescriptionLabel != null:
		$ExhibitionDescriptionLabel.text = ExhibitionStore.get_exhibition_description(exhibitionId)

	SignalBus.trigger_with_payload(SignalBus.SignalType.UPDATE_EXHIBITION_NAVIGATION, exhibitionId)


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.SWITCH_TOPIC, switch_topic)


func setup(exhibitionIdInput: String):
	exhibitionId = exhibitionIdInput
	switch_topic(0) # Load the first topic of the exhibition


func switch_topic(topicIndex: int):
	if currentTopic:
		remove_child(currentTopic)
		currentTopic.queue_free()
		
	currentTopic = load("res://scenes/exhibition/default_topic.tscn").instantiate()
	
	currentTopic.setup(exhibitionId, topicIndex)
	
	add_child(currentTopic)
