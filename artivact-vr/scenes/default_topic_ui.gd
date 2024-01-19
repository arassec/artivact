extends Control


func _on_next_topic_pressed():
	SignalBus.trigger(SignalBus.SignalType.NEXT_TOPIC)


func _on_previous_topic_pressed():
	SignalBus.trigger(SignalBus.SignalType.PREVIOUS_TOPIC)
