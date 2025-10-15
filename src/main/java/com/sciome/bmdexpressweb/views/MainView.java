package com.sciome.bmdexpressweb.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        // Page title
        H1 title = new H1("BMDExpress Web Application");

        // Text field
        TextField nameField = new TextField("Enter your name");
        nameField.setPlaceholder("Your name here");

        // Button
        Button greetButton = new Button("Greet", event -> {
            String name = nameField.getValue();
            if (name == null || name.isEmpty()) {
                Notification.show("Please enter a name!");
            } else {
                Notification.show("Hello, " + name + "! Welcome to BMDExpress Web.");
            }
        });
        greetButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Layout configuration
        setMargin(true);
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.START);

        // Add components to layout
        add(title, nameField, greetButton);
    }
}
