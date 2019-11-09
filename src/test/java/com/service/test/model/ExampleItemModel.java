package com.service.test.model;

/**
 * Example data model that represents an item. Different collections can have
 * different structures. API is flexible to be used with different
 * item models.
 * 
 * @author oozdikis
 */
public class ExampleItemModel {

	private String name;
	private String description;

	public ExampleItemModel() {
	}

	public ExampleItemModel(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
