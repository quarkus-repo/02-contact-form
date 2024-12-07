package org.acme;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import java.util.List;

@Path("/api/contact")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@OpenAPIDefinition(info = @Info(title = "Contact API", version = "1.0"))
public class ContactResource {

    @Inject
    EntityManager entityManager;

    @GET
    @APIResponse(responseCode = "200", description = "List all contacts", content = @Content(schema = @Schema(implementation = Contact.class)))
    public List<Contact> getAllContacts() {
        return entityManager.createQuery("from Contact", Contact.class).getResultList();
    }

    @POST
    @Transactional
    @APIResponse(responseCode = "201", description = "Contact created")
    @APIResponse(responseCode = "400", description = "Invalid input")
    @APIResponse(responseCode = "409", description = "Conflict, contact already exists")
    public Response addContact(Contact contact) {
        if (contact == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Contact data is required").build();
        }

        if (contact.getId() != null) {
            var existingContact = entityManager.find(Contact.class, contact.getId());
            if (existingContact != null) {
                return Response.status(Response.Status.CONFLICT).entity("Contact with ID " + contact.getId() + " already exists").build();
            }
            // En caso de que quieras simplemente ignorar el ID enviado y crear un nuevo contacto
            contact.setId(null); // Explicitly set ID to null to avoid any confusion
        }

        entityManager.persist(contact);
        entityManager.flush();
        return Response.status(Response.Status.CREATED).entity(contact).build();
    }

    @GET
    @Path("/{id}")
    @APIResponse(responseCode = "200", description = "Fetch a contact", content = @Content(schema = @Schema(implementation = Contact.class)))
    @APIResponse(responseCode = "404", description = "Contact not found")
    public Response getContact(@PathParam("id") Long id) {
        Contact contact = entityManager.find(Contact.class, id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(contact).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    @APIResponse(responseCode = "200", description = "Contact updated", content = @Content(schema = @Schema(implementation = Contact.class)))
    @APIResponse(responseCode = "404", description = "Contact not found")
    public Response updateContact(@PathParam( "id") Long id, Contact contact) {
        Contact existingContact = entityManager.find(Contact.class, id);
        if (existingContact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        existingContact.setName(contact.getName());
        existingContact.setEmail(contact.getEmail());
        entityManager.merge(existingContact);
        return Response.ok(existingContact).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    @APIResponse(responseCode = "204", description = "Contact deleted")
    @APIResponse(responseCode = "404", description = "Contact not found")
    public Response deleteContact(@PathParam("id") Long id) {
        Contact contact = entityManager.find(Contact.class, id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entityManager.remove(contact);
        return Response.noContent().build();
    }
}