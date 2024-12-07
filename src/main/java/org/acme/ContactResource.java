package org.acme;

import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;

import java.util.List;

@Path("/api/contact")
@Produces("application/json")
@Consumes("application/json")
public class ContactResource {

    @Inject
    EntityManager entityManager;

    @GET
    public List<Contact> getAllContacts() {
        return entityManager.createQuery("from Contact", Contact.class).getResultList();
    }

    @POST
    public Response addContact(Contact contact) {
        if (contact == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Contact data is required").build();
        }
        entityManager.persist(contact);
        entityManager.flush(); // Ensure ID is generated
        return Response.status(Response.Status.CREATED).entity(contact).build();
    }

    @GET
    @Path("/{id}")
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
    public Response updateContact(@PathParam("id") Long id, Contact contact) {
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
    public Response deleteContact(@PathParam("id") Long id) {
        Contact contact = entityManager.find(Contact.class, id);
        if (contact == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        entityManager.remove(contact);
        return Response.noContent().build(); // 204 No Content
    }

}
