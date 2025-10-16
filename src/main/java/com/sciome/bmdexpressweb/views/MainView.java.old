package com.sciome.bmdexpressweb.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class MainView extends VerticalLayout {

    public MainView() {
        // Page title
        H1 title = new H1("BMDExpress Web Application");

        // Description
        Paragraph description = new Paragraph(
            "A Spring Boot/Vaadin web application for analyzing dose-response data " +
            "using EPA BMDS software and ToxicR models."
        );

        // Documentation section
        H2 docsTitle = new H2("Documentation");
        Anchor docsLink = new Anchor("/docs/index.html", "View Project Documentation");
        docsLink.setTarget("_blank");

        Button docsButton = new Button("Open Documentation", event -> {
            getUI().ifPresent(ui -> ui.getPage().open("/docs/index.html", "_blank"));
        });
        docsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Demo section
        H2 demoTitle = new H2("Demo");
        TextField nameField = new TextField("Enter your name");
        nameField.setPlaceholder("Your name here");

        Button greetButton = new Button("Greet", event -> {
            String name = nameField.getValue();
            if (name == null || name.isEmpty()) {
                Notification.show("Please enter a name!");
            } else {
                Notification.show("Hello, " + name + "! Welcome to BMDExpress Web.");
            }
        });
        greetButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST);

        // Layout configuration
        setMargin(true);
        setPadding(true);
        setSpacing(true);
        setAlignItems(Alignment.START);
        setWidth("800px");

        // Add components to layout
        add(
            title,
            description,
            docsTitle,
            docsButton,
            demoTitle,
            nameField,
            greetButton
        );
    }
}
