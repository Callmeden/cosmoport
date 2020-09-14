package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rest/ships")
public class ShipController {

    private final ShipService shipService;


    @Autowired
    public ShipController(ShipService shipService) {
        this.shipService = shipService;
    }

    @GetMapping
    public ResponseEntity<List<Ship>> getShipsList(@RequestParam(required = false) String name, @RequestParam(required = false) String planet, @RequestParam(required = false) ShipType shipType,
                                          @RequestParam(required = false) Long after, @RequestParam(required = false) Long before, @RequestParam(required = false) Boolean isUsed,
                                          @RequestParam(required = false) Double minSpeed, @RequestParam(required = false) Double maxSpeed,
                                          @RequestParam(required = false) Integer minCrewSize, @RequestParam(required = false) Integer maxCrewSize,
                                          @RequestParam(required = false) Double minRating, @RequestParam(required = false) Double maxRating,
                                          @RequestParam(required = false) ShipOrder order,
                                          @RequestParam(required = false) Integer pageNumber, @RequestParam(required = false) Integer pageSize){


        List<Ship> resultShipList = shipService.getShipsList(name,planet,shipType,after,
                before,isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);


        return new ResponseEntity<>(resultShipList,HttpStatus.OK);
    }


    @GetMapping(value = "/count")
    public ResponseEntity<Integer> getShipsCount(@RequestParam(required = false) String name, @RequestParam(required = false) String planet, @RequestParam(required = false) ShipType shipType,
                                          @RequestParam(required = false) Long after, @RequestParam(required = false) Long before, @RequestParam(required = false) Boolean isUsed,
                                          @RequestParam(required = false) Double minSpeed, @RequestParam(required = false) Double maxSpeed,
                                          @RequestParam(required = false) Integer minCrewSize, @RequestParam(required = false) Integer maxCrewSize,
                                          @RequestParam(required = false) Double minRating, @RequestParam(required = false) Double maxRating){

        Integer count = shipService.getShipsCount(name,planet,shipType,after,
                before,isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        return new ResponseEntity<>(count,HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Ship> createShip(@RequestBody Ship ship){
        final Ship result = shipService.createShip(ship);
        return result != null
                ? new ResponseEntity<>(result,HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<Ship> getShip(@PathVariable String id){
        if (!isIdValid(id))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        Ship ship = shipService.getShip(Long.parseLong(id));

        return ship  != null
                ? new ResponseEntity<>(ship, HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }



    @PostMapping(value = "/{id}")
    public ResponseEntity<Ship> updateShip(@PathVariable String id, @RequestBody Ship ship){
        if(!isIdValid(id))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return ship!=null
                ? shipService.updateShip(Long.valueOf(id),ship)
                : new ResponseEntity<>(shipService.getShip(Long.valueOf(id)),HttpStatus.OK);
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<?> deleteShip(@PathVariable String id){
        if(!isIdValid(id))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

        return shipService.deleteShip(Long.valueOf(id))
                ? new ResponseEntity<>(HttpStatus.OK)
                : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    private Boolean isIdValid(String id){
        try {
            long idLong = Long.parseLong(id);
            if(idLong <= 0)
                throw new NumberFormatException();
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
