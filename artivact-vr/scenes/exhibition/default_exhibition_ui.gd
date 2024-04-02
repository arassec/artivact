extends Node

func _init():
	SignalBus.register(SignalBus.SignalType.UPDATE_EXHIBITION_NAVIGATION, update_exhibition_navigation)


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.UPDATE_EXHIBITION_NAVIGATION, update_exhibition_navigation)


func update_exhibition_navigation(exhibitionIdInput: String):
	var topicsContainer = find_child("TopicsContainer")
	var topicIndex = 0
	for topic in ExhibitionStore.get_exhibition(exhibitionIdInput).topics:
		var topicButton = Button.new()
		topicButton.text = I18n.translate(topic.title)
		topicButton.pressed.connect(trigger_topic_switch.bind(topicIndex))
		
		var topicButtonContainer = MarginContainer.new()
		topicButtonContainer.add_theme_constant_override("margin_left", 10)
		topicButtonContainer.add_theme_constant_override("margin_right", 10)
		topicButtonContainer.add_theme_constant_override("margin_top", 5)
		topicButtonContainer.add_theme_constant_override("margin_bottom", 5)
		topicButtonContainer.add_child(topicButton)
		
		topicsContainer.add_child(topicButtonContainer);
		
		topicIndex = topicIndex + 1


func trigger_topic_switch(topicIndex: int):
	SignalBus.trigger_with_payload(SignalBus.SignalType.SWITCH_TOPIC, topicIndex)


func close_exhibition():
	SignalBus.trigger(SignalBus.SignalType.CLOSE_EXHIBITION)
