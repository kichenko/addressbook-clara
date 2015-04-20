package com.vaadin.tutorial.addressbook;

import org.vaadin.teemu.clara.Clara;
import org.vaadin.teemu.clara.binder.annotation.UiDataSource;
import org.vaadin.teemu.clara.binder.annotation.UiField;
import org.vaadin.teemu.clara.binder.annotation.UiHandler;

import com.vaadin.annotations.Title;
import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;

/* 
 * UI class is the starting point for your app. You may deploy it with VaadinServlet
 * or VaadinPortlet by giving your UI class name a parameter. When you browse to your
 * app a web page showing your UI is automatically generated. Or you may choose to 
 * embed your UI to an existing web page. 
 */
@SuppressWarnings({ "serial", "unchecked" })
@Title("Addressbook")
public class AddressbookUI extends UI {

    /* User interface components are stored in session. */
    @UiField("contactList")
    private Table contactList;

    @UiField("searchField")
    private TextField searchField;

    @UiField("removeContactButton")
    private Button removeContactButton;

    @UiField("editorLayout")
    private FormLayout editorLayout;

    private FieldGroup editorFields = new FieldGroup();

    private static final String FNAME = "First Name";
    private static final String LNAME = "Last Name";
    private static final String COMPANY = "Company";
    private static final String[] fieldNames = new String[] { FNAME, LNAME,
            COMPANY, "Mobile Phone", "Work Phone", "Home Phone", "Work Email",
            "Home Email", "Street", "City", "Zip", "State", "Country" };

    /*
     * Any component can be bound to an external data source. This example uses
     * just a dummy in-memory list, but there are many more practical
     * implementations.
     */
    IndexedContainer contactContainer;

    /*
     * After UI class is created, init() is executed. You should build and wire
     * up your user interface here.
     */
    @Override
    protected void init(VaadinRequest request) {
        /*
         * Load the content layout from the XML resource file and bind to this
         * object via annotations (@UiField, @UiDataSource, @UiHandler).
         */
        setContent(Clara.create("AddressbookUI.xml", this));
        contactContainer = (IndexedContainer) contactList
                .getContainerDataSource();

        initContactList();
        initEditor();
    }

    private void initEditor() {
        /* User interface can be created dynamically to reflect underlying data. */
        int index = 1;
        for (String fieldName : fieldNames) {
            TextField field = new TextField(fieldName);
            editorLayout.addComponent(field, index++);
            field.setWidth("100%");

            /*
             * We use a FieldGroup to connect multiple components to a data
             * source at once.
             */
            editorFields.bind(field, fieldName);
        }

        /*
         * Data can be buffered in the user interface. When doing so, commit()
         * writes the changes to the data source. Here we choose to write the
         * changes automatically without calling commit().
         */
        editorFields.setBuffered(false);
    }

    @UiHandler("searchField")
    public void doSearch(final TextChangeEvent event) {
        /* Reset the filter for the contactContainer. */
        contactContainer.removeAllContainerFilters();
        contactContainer.addContainerFilter(new ContactFilter(event.getText()));
    }

    @UiHandler("removeContactButton")
    public void removeSelectedContact(ClickEvent event) {
        Object contactId = contactList.getValue();
        contactList.removeItem(contactId);
    }

    @UiHandler("newContactButton")
    public void addNewContact(ClickEvent event) {

        /*
         * Rows in the Container data model are called Item. Here we add a new
         * row in the beginning of the list.
         */
        contactContainer.removeAllContainerFilters();
        Object contactId = contactContainer.addItemAt(0);

        /*
         * Each Item has a set of Properties that hold values. Here we set a
         * couple of those.
         */
        contactList.getContainerProperty(contactId, FNAME).setValue("New");
        contactList.getContainerProperty(contactId, LNAME).setValue("Contact");

        /* Lets choose the newly created contact to edit it. */
        contactList.select(contactId);
    }

    private void initContactList() {
        contactList.setVisibleColumns(new String[] { FNAME, LNAME, COMPANY });
    }

    @UiHandler("contactList")
    public void contactSelected(ValueChangeEvent event) {
        Object contactId = contactList.getValue();

        /*
         * When a contact is selected from the list, we want to show that in our
         * editor on the right. This is nicely done by the FieldGroup that binds
         * all the fields to the corresponding Properties in our contact at
         * once.
         */
        if (contactId != null) {
            editorFields.setItemDataSource(contactList.getItem(contactId));
        }

        editorLayout.setVisible(contactId != null);
    }

    /*
     * Generate some in-memory example data to play with. In a real application
     * we could be using SQLContainer, JPAContainer or some other to persist the
     * data.
     */
    @UiDataSource("contactList")
    public IndexedContainer createDummyDatasource() {
        IndexedContainer ic = new IndexedContainer();

        for (String p : fieldNames) {
            ic.addContainerProperty(p, String.class, "");
        }

        /* Create dummy data by randomly combining first and last names */
        String[] fnames = { "Peter", "Alice", "Joshua", "Mike", "Olivia",
                "Nina", "Alex", "Rita", "Dan", "Umberto", "Henrik", "Rene",
                "Lisa", "Marge" };
        String[] lnames = { "Smith", "Gordon", "Simpson", "Brown", "Clavel",
                "Simons", "Verne", "Scott", "Allison", "Gates", "Rowling",
                "Barks", "Ross", "Schneider", "Tate" };
        for (int i = 0; i < 1000; i++) {
            Object id = ic.addItem();
            ic.getContainerProperty(id, FNAME).setValue(
                    fnames[(int) (fnames.length * Math.random())]);
            ic.getContainerProperty(id, LNAME).setValue(
                    lnames[(int) (lnames.length * Math.random())]);
        }

        return ic;
    }

    /*
     * A custom filter for searching names and companies in the
     * contactContainer.
     */
    private static class ContactFilter implements Filter {
        private String needle;

        public ContactFilter(String needle) {
            this.needle = needle.toLowerCase();
        }

        public boolean passesFilter(Object itemId, Item item) {
            String haystack = ("" + item.getItemProperty(FNAME).getValue()
                    + item.getItemProperty(LNAME).getValue() + item
                    .getItemProperty(COMPANY).getValue()).toLowerCase();
            return haystack.contains(needle);
        }

        public boolean appliesToProperty(Object id) {
            return true;
        }
    }
}
