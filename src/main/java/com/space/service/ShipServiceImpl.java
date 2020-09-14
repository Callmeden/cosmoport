package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService {
    private final ShipRepository shipRepository;

    @Autowired
    public ShipServiceImpl(ShipRepository shipRepository) {
        this.shipRepository = shipRepository;
    }

    @Override
    public List<Ship> getShipsList(String name, String planet, ShipType shipType, Long after,
                                   Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                   Integer minCrewSize, Integer maxCrewSize, Double minRating,
                                   Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize) {

       List<Ship> allShipsList = filteringListByAllParams(shipRepository.findAll(),name,planet,shipType,after,
                before,isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating);

        if(pageNumber == null)
            pageNumber = 0;

        if(pageSize == null)
            pageSize = 3;

        if(order == null)
            allShipsList.sort(Comparator.comparingLong(Ship::getId));
        else

            switch(order.getFieldName()){

                case "speed" : allShipsList.sort(Comparator.comparingDouble(Ship::getSpeed)); break;

                case "prodDate" : allShipsList.sort(Comparator.comparingLong(o -> o.getProdDate().getTime())); break;

                case "rating" : allShipsList.sort(Comparator.comparingDouble(Ship::getRating)); break;

                default: allShipsList.sort(Comparator.comparingLong(Ship::getId)); break;
            }

        List<Ship> resultShipList = new ArrayList<>();

        for(int i = pageNumber * pageSize; i < (pageNumber+1) * pageSize; i++)
            if(i<allShipsList.size())
                resultShipList.add(allShipsList.get(i));

        return resultShipList;
    }

    @Override
    public Integer getShipsCount(String name, String planet, ShipType shipType, Long after, Long before,
                                 Boolean isUsed, Double minSpeed, Double maxSpeed,
                                 Integer minCrewSize, Integer maxCrewSize, Double minRating, Double maxRating) {
        return filteringListByAllParams(shipRepository.findAll(),name,planet,shipType,after,
                before,isUsed, minSpeed, maxSpeed, minCrewSize, maxCrewSize, minRating, maxRating).size();
    }


    @Override
    public Ship createShip(Ship ship) {
        if(ship.getName()==null || ship.getPlanet()==null || ship.getShipType() == null || ship.getProdDate() == null || ship.getSpeed()==null || ship.getCrewSize() == null
                || ship.getName().length()>50 || ship.getPlanet().length()>50 || ship.getName().isEmpty() || ship.getPlanet().isEmpty()
                || ship.getCrewSize()<1 || ship.getCrewSize()>9999 || ship.getProdDate().getTime()<0) {
            return null;
        }

        double roundedSpeed = Math.round(ship.getSpeed()*100.0)/ 100.0;
        if(roundedSpeed<0.01 || roundedSpeed > 0.99) {
            return null;
        }
        else ship.setSpeed(roundedSpeed);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ship.getProdDate());
        if(calendar.get(Calendar.YEAR)<2800 || calendar.get(Calendar.YEAR)>3019) {
            return null;
        }

        if(ship.getUsed()==null)
            ship.setUsed(false);

        calculateAndSetRating(ship,calendar);

        return shipRepository.save(ship);
    }

    @Override
    public Ship getShip(Long id) {

        return shipRepository.existsById(id)
                ? shipRepository.findById(id).get()
                : null;
    }

    @Override
    public ResponseEntity<Ship> updateShip(Long id, Ship ship) {
        if(!shipRepository.existsById(id))
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        Ship oldShip = shipRepository.findById(id).get();

        if(ship == null)
            return new ResponseEntity<>(oldShip, HttpStatus.OK);

        if(ship.getName()!=null)
            if(ship.getName().length()>50 || ship.getName().isEmpty())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else
            oldShip.setName(ship.getName());

        if(ship.getPlanet() != null )
            if(ship.getPlanet().length() > 50 || ship.getPlanet().isEmpty())
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else
            oldShip.setPlanet(ship.getPlanet());

        if(ship.getShipType() != null)
            oldShip.setShipType(ship.getShipType());

        if(ship.getProdDate() != null)
            if(ship.getProdDate().getTime()<0)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(ship.getProdDate());
                if (calendar.get(Calendar.YEAR) < 2800 || calendar.get(Calendar.YEAR) > 3019) {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
                oldShip.setProdDate(calendar.getTime());
            }

        if(ship.getUsed() != null)
            oldShip.setUsed(ship.getUsed());

        if(ship.getSpeed() != null) {
            double roundedSpeed = Math.round(ship.getSpeed()*100.0)/ 100.0;
            if(roundedSpeed<0.01 || roundedSpeed > 0.99) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
            oldShip.setSpeed(roundedSpeed);
        }
        if(ship.getCrewSize() != null)
            if(ship.getCrewSize()<1 || ship.getCrewSize()>9999)
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            else
            oldShip.setCrewSize(ship.getCrewSize());

        if(ship.getProdDate()!=null || ship.getUsed()!= null || ship.getSpeed()!=null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(oldShip.getProdDate());
            calculateAndSetRating(oldShip,calendar);
        }

        Ship newShip = shipRepository.save(oldShip);
        return new ResponseEntity<>(newShip,HttpStatus.OK);
    }

    @Override
    public Boolean deleteShip(Long id) {
        if(!shipRepository.existsById(id))
            return false;
        shipRepository.deleteById(id);
        return true;
    }

    private void calculateAndSetRating(Ship ship, Calendar calendar){
        Double k = ship.getUsed() ? 0.5 : 1;
        Double v = ship.getSpeed();
        int yearDifference = 3019 - calendar.get(Calendar.YEAR) + 1;
        Double rating = Math.round((k * v * 80 / (double) yearDifference) * 100.0) / 100.0;
        ship.setRating(rating);
    }

    private List<Ship> filteringListByAllParams(List<Ship> shipList, String name, String planet, ShipType shipType, Long after,
                                                Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                                                Integer minCrewSize, Integer maxCrewSize, Double minRating,
                                                Double maxRating){
        List<Ship> bufferList = shipList;
        List<Ship> lastEditedList = bufferList;
        if(name!=null) {
            bufferList = editListByName(lastEditedList, name);
            lastEditedList = bufferList;
        }

        if(planet!=null) {
            bufferList = editListByPlanet(lastEditedList, planet);
            lastEditedList = bufferList;
        }

        if(shipType!=null) {
            bufferList = editListByShipType(lastEditedList, shipType);
            lastEditedList = bufferList;
        }

        if(after!=null) {
            bufferList = editListByAfter(lastEditedList, after);
            lastEditedList = bufferList;
        }

        if(before!=null) {
            bufferList = editListByBefore(lastEditedList, before);
            lastEditedList = bufferList;
        }

        if(isUsed!=null) {
            bufferList = editListByIsUsed(lastEditedList, isUsed);
            lastEditedList = bufferList;
        }

        if(minSpeed!=null) {
            bufferList = editListByMinSpeed(lastEditedList, minSpeed);
            lastEditedList = bufferList;
        }

        if(maxSpeed!=null) {
            bufferList = editListByMaxSpeed(lastEditedList, maxSpeed);
            lastEditedList = bufferList;
        }

        if(minCrewSize!=null) {
            bufferList = editListByMinCrewSize(lastEditedList, minCrewSize);
            lastEditedList = bufferList;
        }

        if(maxCrewSize!=null) {
            bufferList = editListByMaxCrewSize(lastEditedList, maxCrewSize);
            lastEditedList = bufferList;
        }

        if(minRating!=null) {
            bufferList = editListByMinRating(lastEditedList, minRating);
            lastEditedList = bufferList;
        }

        if(maxRating!=null) {
            bufferList = editListByMaxRating(lastEditedList, maxRating);
            lastEditedList = bufferList;
        }

        return lastEditedList;
    }

    private List<Ship> editListByName(List<Ship> shipList, String name){

        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if (ship.getName().contains(name))
                result.add(ship);
        }

        return result;
    }

    private List<Ship> editListByPlanet(List<Ship> shipList, String planet){

        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if (ship.getPlanet().contains(planet))
                result.add(ship);
        }

        return result;
    }

    private List<Ship> editListByShipType(List<Ship> shipList, ShipType shipType){

        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if(ship.getShipType().equals(shipType))
                result.add(ship);
        }

        return result;
    }

    private List<Ship> editListByAfter(List<Ship> shipList, Long after){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if(ship.getProdDate().getTime() >= after)
                result.add(ship);
        }

        return result;
    }
    private List<Ship> editListByBefore(List<Ship> shipList, Long before){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if(ship.getProdDate().getTime() <= before)
                result.add(ship);
        }

        return result;
    }

    private List<Ship> editListByIsUsed(List<Ship> shipList, Boolean isUsed){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if(ship.getUsed().equals(isUsed))
                result.add(ship);
        }

        return result;
    }

    private List<Ship> editListByMinSpeed(List<Ship> shipList, Double minSpeed) {
        List<Ship> result = new ArrayList<>();

        for (Ship ship : shipList){
            if (ship.getSpeed() >= minSpeed)
                result.add(ship);
        }

        return result;
    }
    private List<Ship> editListByMaxSpeed(List<Ship> shipList, Double maxSpeed){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if (ship.getSpeed() <= maxSpeed)
                result.add(ship);
        }

        return result;
    }

    private List<Ship> editListByMinCrewSize(List<Ship> shipList, Integer minCrewSize){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if(ship.getCrewSize() >= minCrewSize)
                result.add(ship);
        }

        return result;
    }
    private List<Ship> editListByMaxCrewSize(List<Ship> shipList, Integer maxCrewSize){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if(ship.getCrewSize() <= maxCrewSize)
                result.add(ship);
        }

        return result;
    }

    private List<Ship> editListByMinRating(List<Ship> shipList, Double minRating){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if(ship.getRating() >= minRating)
                result.add(ship);
        }

        return result;
    }
    private List<Ship> editListByMaxRating(List<Ship> shipList, Double maxRating){
        List<Ship> result = new ArrayList<>();

        for(Ship ship : shipList){
            if (ship.getRating() <= maxRating)
                result.add(ship);
        }

        return result;
    }
}
