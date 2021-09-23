package com.example.infleranspringrestapi.event;

import com.example.infleranspringrestapi.index.IndexController;
import org.aspectj.asm.IModelFilter;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Controller
@RequestMapping(value = "/api/events", produces = MediaTypes.HAL_JSON_VALUE)
public class EventController {

    private final EventRepository eventRepository;
    private final ModelMapper modelMapper;
    private final EventValidator eventValidator;

    public EventController(EventRepository eventRepository, ModelMapper modelMapper, EventValidator eventValidator) {
        this.eventRepository = eventRepository;
        this.modelMapper = modelMapper;
        this.eventValidator = eventValidator;
    }

    @PostMapping
    public ResponseEntity createEvent(@RequestBody @Valid EventDto eventDto, Errors errors) {
        if (errors.hasErrors()) {
            EntityModel<Errors> errorsEntityModel = EntityModel.of(errors);
            errorsEntityModel.add(linkTo(methodOn(IndexController.class).index()).withRel("index"));
            return ResponseEntity.badRequest().body(errorsEntityModel);
        }

        eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            // 위의 내용과 실제 구현 내용은 동일함.
            return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
        }

        Event event = modelMapper.map(eventDto, Event.class);
        event.update();
        Event newEvent = eventRepository.save(event);

//      URI createdUri = linkTo(EventController.class).slash("{id}").toUri();
        WebMvcLinkBuilder linkBuilder = linkTo(EventController.class).slash(newEvent.getId());

        EntityModel<Event> model = EntityModel.of(newEvent);
        model.add(linkBuilder.withSelfRel());
        model.add(linkBuilder.withRel("query-events"));
        model.add(linkBuilder.withRel("update-event"));
        model.add(linkTo(EventController.class).slash("docs/index.html#resources-events-create").withRel("profile"));

        return ResponseEntity.created(linkBuilder.toUri()).body(model);
    }

    @GetMapping
    public ResponseEntity<?> queryEvents(Pageable pageable, PagedResourcesAssembler<Event> assembler ) {
        Page<Event> page = this.eventRepository.findAll(pageable);
        PagedModel<EntityModel<Event>> entityModels = assembler.toModel(page, EventResource::new);
        entityModels.add(linkTo(EventController.class).slash("docs/index.html#resources-events-list").withRel("profile"));
        return ResponseEntity.ok(entityModels);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Event>> getEvent(@PathVariable Integer id) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Event event = optionalEvent.get();
        EntityModel<Event> entityModel = EntityModel.of(event);
        entityModel.add(linkTo(EventController.class).withSelfRel());
        entityModel.add(linkTo(EventController.class).withRel("update-event"));
        entityModel.add(linkTo(EventController.class).slash("docs/index.html#resources-events-get").withRel("profile"));

        return ResponseEntity.ok(entityModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Integer id,
                                      @RequestBody @Valid EventDto eventDto,
                                      Errors errors) {
        Optional<Event> optionalEvent = this.eventRepository.findById(id);
        if (optionalEvent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
        }

        this.eventValidator.validate(eventDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorsResource.modelOf(errors));
        }

        Event existingEvent = optionalEvent.get();
        this.modelMapper.map(eventDto, existingEvent);
        Event savedEvent = this.eventRepository.save(existingEvent);

        EntityModel<Event> entityModel = EntityModel.of(savedEvent);
        entityModel.add(linkTo(EventController.class).withSelfRel());
        entityModel.add(linkTo(EventController.class).withRel("query-events"));
        entityModel.add(linkTo(EventController.class).slash("docs/index.html#resources-events-update").withRel("profile"));

        return ResponseEntity.ok(entityModel);
    }
}
