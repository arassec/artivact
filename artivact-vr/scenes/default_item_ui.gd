extends Control

func _enter_tree():
	SignalBus.register(SignalBus.SignalType.UPDATE_ITEM_DATA, update_item_data)


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.UPDATE_ITEM_DATA, update_item_data)


func update_item_data(itemDataInput: Variant):
	find_child("TitleLabel").text = itemDataInput.title.value
	find_child("DescriptionLabel").text = itemDataInput.description.value
	_create_property_category_tabs(itemDataInput)


func _create_property_category_tabs(itemDataInput: Variant):
	var categoryTabs = find_child("CategoryPropertiesTabContainer")
	var propertyCategories = ArtivactSettings.get_property_categories()
	for propertyCategory in propertyCategories:
		var propertyContainer = _create_property_category_tab_content(propertyCategory, itemDataInput)
		categoryTabs.add_child(propertyContainer)


func _create_property_category_tab_content(propertyCategory: Variant, itemDataInput: Variant):
	var marginContainer = MarginContainer.new()
	marginContainer.name = propertyCategory.value # TODO: I18N
	marginContainer.add_theme_constant_override("margin_left", 20)
	marginContainer.add_theme_constant_override("margin_top", 20)
	
	var categoryContainer = GridContainer.new()
	categoryContainer.columns = 2
	categoryContainer.add_theme_constant_override("h_separation", 25)
	categoryContainer.add_theme_constant_override("v_separation", 10)

	for propertyDefinition in propertyCategory.properties:
		var propertyKeyLabel = Label.new()
		propertyKeyLabel.text = propertyDefinition.value # TODO: I18N
		propertyKeyLabel.add_theme_font_size_override("font_size", 24)
		categoryContainer.add_child(propertyKeyLabel)

		if itemDataInput.properties.has(propertyDefinition.id):
			var propertyValueLabel = Label.new()
			propertyValueLabel.text = itemDataInput.properties[propertyDefinition.id]
			propertyValueLabel.add_theme_font_size_override("font_size", 24)
			categoryContainer.add_child(propertyValueLabel)
		else:
			var emptyLabel = Label.new();
			categoryContainer.add_child(emptyLabel);

	marginContainer.add_child(categoryContainer)

	return marginContainer
