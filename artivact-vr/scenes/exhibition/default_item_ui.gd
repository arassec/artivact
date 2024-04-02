extends Control

func _init():
	SignalBus.register(SignalBus.SignalType.UPDATE_ITEM_DATA, update_item_data)


func _exit_tree():
	SignalBus.deregister(SignalBus.SignalType.UPDATE_ITEM_DATA, update_item_data)


func update_item_data(exhibitionIdInput: String, itemDataInput: Variant):
		find_child("TitleLabel").text = I18n.translate(itemDataInput.title)
		find_child("DescriptionLabel").text = I18n.translate(itemDataInput.description)
		create_property_category_tabs(exhibitionIdInput, itemDataInput)


func create_property_category_tabs(exhibitionIdInput: String, itemDataInput: Variant):
	var categoryTabs = find_child("CategoryPropertiesTabContainer")
	for tab in categoryTabs.get_children():
		categoryTabs.remove_child(tab)
		tab.queue_free()
	var propertyCategories = ExhibitionStore.get_property_categories(exhibitionIdInput)
	for propertyCategory in propertyCategories:
		var propertyContainer = create_property_category_tab_content(propertyCategory, itemDataInput)
		categoryTabs.add_child(propertyContainer)


func create_property_category_tab_content(propertyCategory: Variant, itemDataInput: Variant):
	var marginContainer = MarginContainer.new()
	marginContainer.name = I18n.translate(propertyCategory)
	marginContainer.add_theme_constant_override("margin_left", 20)
	marginContainer.add_theme_constant_override("margin_top", 20)
	
	var categoryContainer = GridContainer.new()
	categoryContainer.columns = 2
	categoryContainer.add_theme_constant_override("h_separation", 25)
	categoryContainer.add_theme_constant_override("v_separation", 10)

	for propertyDefinition in propertyCategory.properties:
		var propertyKeyLabel = Label.new()
		propertyKeyLabel.text = I18n.translate(propertyDefinition)
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
