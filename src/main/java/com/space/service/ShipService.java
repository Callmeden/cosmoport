package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ShipService {

    /**
     * Возвращает список всех имеющихся кораблей по параметрам фильтра
     * @return - список кораблей
     */
    List<Ship> getShipsList(String name, String planet, ShipType shipType, Long after,
                            Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                            Integer minCrewSize, Integer maxCrewSize, Double minRating,
                            Double maxRating, ShipOrder order, Integer pageNumber, Integer pageSize);

    /**
     * @return - количество кораблей с такими параметрами фильтра
     */

    Integer getShipsCount(String name, String planet, ShipType shipType, Long after,
                          Long before, Boolean isUsed, Double minSpeed, Double maxSpeed,
                          Integer minCrewSize, Integer maxCrewSize, Double minRating,
                          Double maxRating);

    /**
     * Создаёт корабль
     * @param ship - создаваемый корабль
     * @return - объект созданного корабля
     */
    Ship createShip(Ship ship);

    /**
     * Возвращает корабль по ID
     * @param id - ID запрошенного корабля
     * @return - объект корабля
     */
    Ship getShip(Long id);

    /**
     * Обновляем данные о корабле
     * @param ship - корабль с обновлёнными данными
     * @param id - ID корабля
     */
    ResponseEntity<Ship> updateShip(Long id, Ship ship);

    /**
     * Удаление корабля по его ID
     * @param id - ID корабля для удаления
     */
    Boolean deleteShip(Long id);
}
