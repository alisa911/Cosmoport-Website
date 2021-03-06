package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipPojo;
import com.space.model.ShipType;
import com.space.service.ShipService;
import com.space.service.ShipValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.util.*;


@Controller
public class ShipController {
    @InitBinder("ship")
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(new ShipValidator());
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport() {
            public void setAsText(String value) {
                setValue(new Date(Long.valueOf(value)));
            }

        });
    }

    @Autowired
    private ShipService shipService;
    @Transactional
    @GetMapping("/rest/ships/count")
    public ResponseEntity<?> getShipsCount(@RequestParam (value = "name", required = false) String name,
                                 @RequestParam (value = "planet", required = false) String planet,
                                 @RequestParam (value = "shipType", required = false) ShipType shipType,
                                 @RequestParam (value = "after", required = false) Long after,
                                 @RequestParam (value = "before", required = false) Long before,
                                 @RequestParam (value = "isUsed", required = false) Boolean isUsed,
                                 @RequestParam (value = "minSpeed", required = false) Double minSpeed,
                                 @RequestParam (value = "maxSpeed", required = false) Double maxSpeed,
                                 @RequestParam (value = "minCrewSize", required = false) Integer minCrewSize,
                                 @RequestParam (value = "maxCrewSize", required = false) Integer maxCrewSize,
                                 @RequestParam (value = "minRating", required = false) Double minRating,
                                 @RequestParam (value = "maxRating", required = false) Double maxRating,
                                 @RequestParam (value = "order", defaultValue = "ID" ) ShipOrder order
                                 ){
        Integer count = shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order).size();
        return new ResponseEntity<>(count,HttpStatus.OK);
    }

    @Transactional
    @GetMapping("/rest/ships")
    public  ResponseEntity<?> getShipsList(
            @RequestParam (value = "name", required = false) String name,
            @RequestParam (value = "planet", required = false) String planet,
            @RequestParam (value = "shipType", required = false) ShipType shipType,
            @RequestParam (value = "after", required = false) Long after,
            @RequestParam (value = "before", required = false) Long before,
            @RequestParam (value = "isUsed", required = false) Boolean isUsed,
            @RequestParam (value = "minSpeed", required = false) Double minSpeed,
            @RequestParam (value = "maxSpeed", required = false) Double maxSpeed,
            @RequestParam (value = "minCrewSize", required = false) Integer minCrewSize,
            @RequestParam (value = "maxCrewSize", required = false) Integer maxCrewSize,
            @RequestParam (value = "minRating", required = false) Double minRating,
            @RequestParam (value = "maxRating", required = false) Double maxRating,
            @RequestParam (value = "order", defaultValue = "ID" ) ShipOrder order,
            @RequestParam(value = "pageNumber", defaultValue = "0") Integer pageNumber,
            @RequestParam(value = "pageSize", defaultValue = "3") Integer pageSize) {

            List<Ship> findShip = shipService.getShipsList(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order);
            Pageable pagesAndSort = PageRequest.of(pageNumber, pageSize);
            int max = (pageSize * (pageNumber + 1) > findShip.size()) ? findShip.size() : pageSize * (pageNumber + 1);
            Page<Ship> ships = new PageImpl<>(findShip.subList(pageNumber * pageSize, max), pagesAndSort, findShip.size());

            return ResponseEntity.ok().body(ships.getContent());
        }

    @Transactional
    @PostMapping("/rest/ships/")
    public  ResponseEntity<?> createShip (@RequestBody @Valid Ship ship, BindingResult result) {

        if(result.hasErrors()) {
            return new ResponseEntity<>(result.getAllErrors(), HttpStatus.BAD_REQUEST);
        } else {
            shipService.createShip(ship);
            return ResponseEntity.ok().body(ship);
        }
    }
    @Transactional
    @GetMapping("/rest/ships/{id}")
    public ResponseEntity<?> getShip ( @PathVariable Long id) {

        @Valid Ship ship = shipService.getShip(id);
         if (id == 0){
             return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
         }else
        if (ship == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }  else {
            ShipPojo shipPojo = new ShipPojo();
            shipPojo.setId(ship.getId());
            shipPojo.setName(ship.getName());
            shipPojo.setPlanet(ship.getPlanet());
            shipPojo.setShipType(ship.getShipType());
            shipPojo.setProdDate(ship.getProdDate());
            shipPojo.setUsed(ship.getUsed());
            shipPojo.setSpeed(ship.getSpeed());
            shipPojo.setCrewSize(ship.getCrewSize());
            shipPojo.setRating(ship.getRating());

            return ResponseEntity.ok().body(shipPojo);
        }
    }
    @Transactional
    @DeleteMapping("/rest/ships/{id}")
    public ResponseEntity<?> deleteShip(@PathVariable(value="id") @Valid Long id) {
        if (id == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }else
        if (shipService.getShip(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }else
            shipService.deleteShip(id);
            return new ResponseEntity<>(HttpStatus.OK);
    }
    @Transactional
    @PostMapping("/rest/ships/{id}")
    public ResponseEntity<?> updateShip( @Valid @RequestBody ShipPojo ship, @PathVariable Long id ) {

        Ship existingShip = shipService.getShip(id);

        if (id == 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } else if (existingShip == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else if (shipService.getShip(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        if (ship.toString().equals("{nullnullnullnullnullfalsenullnullnull}")){
            return ResponseEntity.ok().body(existingShip);
        }

        existingShip.setId(id);

        if (ship.getName() != null) {
            existingShip.setName(ship.getName());
            if ( existingShip.getName().length()<1){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (ship.getPlanet() != null) {
            existingShip.setPlanet(ship.getPlanet());
            if ( existingShip.getPlanet().length()<1){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (ship.getShipType() != null) {
            existingShip.setShipType(ship.getShipType());
        }
        if (ship.getProdDate() != null) {
            existingShip.setProdDate(ship.getProdDate());
            if (existingShip.getProdDate().compareTo(new Date(2800,01,01))<0 && (existingShip.getProdDate().compareTo(new Date(3019,12,31))>0) || existingShip.getProdDate().getTime()<0 ){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (ship.getUsed() != null) {
            existingShip.setUsed(ship.getUsed());
        }
        if (ship.getSpeed() != null) {
            existingShip.setSpeed(ship.getSpeed());
            if ( ship.getSpeed()<0.01 || ship.getSpeed()>0.99){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (ship.getCrewSize() != null) {
            existingShip.setCrewSize(ship.getCrewSize());
            if ( existingShip.getCrewSize()<1 || existingShip.getCrewSize()>9999){
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        if (ship.getRating() != null) {
            existingShip.setRating(ship.getRating());
        }

        return ResponseEntity.ok().body(shipService.createShip(existingShip));
    }

}
