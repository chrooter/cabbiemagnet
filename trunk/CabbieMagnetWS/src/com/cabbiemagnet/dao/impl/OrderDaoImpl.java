package com.cabbiemagnet.dao.impl;

import java.util.ArrayList;

import org.springframework.jdbc.core.JdbcTemplate;

import com.cabbiemagnet.dao.IOrderDao;
import com.cabbiemagnet.dao.mapper.OrderRowMapper;
import com.cabbiemagnet.dao.mapper.OrderedCarRowMapper;
import com.cabbiemagnet.model.Order;
import com.cabbiemagnet.model.OrderedCar;
/**
 * 
 * ================================================================================
 * @author anlazarov
 * @date Nov 13, 2011
 * @time 6:44:54 PM
 *								
 * @project CabbieMagnetWS	
 * @package com.cabbiemagnet.dao.impl	
 * @filename OrderDaoImpl.java
 * @description 
 * ================================================================================
 */
public class OrderDaoImpl implements IOrderDao {

	private JdbcTemplate jdbcTemplate;

	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@Override
	public void create(Order order) {
		// TODO Auto-generated method stub

	}

	@Override
	public ArrayList<Order> read(long customerId) {

		String sqlGetOrder =  "SELECT CAB_ORDER.ID, "
				+ "CUSTOMER.NAME AS CUSTOMER_NAME, "
				+ "COMPANY.NAME AS COMPANY_NAME, "
				+ "CAB_ORDER_STATE.NAME AS ORDER_STATE, "
				+ "CAB_ORDER.TIME_ORDERED, "
				+ "CAB_ORDER.TIME_TO_DELIVER AS REQUESTED_TIME_TO_DELIVER, "
				+ "CAB_ORDER.FROM_LOCATION, "
				+ "CAB_ORDER.TO_LOCATION, "
				+ "CAB_ORDER.CUSTOMER_NOTE, "
				+ "CAB_ORDER_REPLY.TIME_TO_DELIVER AS ORDER_REPLY_TIME_TO_DELIVER, "
				+ "CAB_ORDER_REPLY.TIME_OF_REPLY AS ORDER_REPLY_TIME, ( "
				+ "SELECT MESSAGE.MESSAGE	"
				+ "FROM CAB_ORDER_REPLY "
				+ "JOIN MESSAGE ON MESSAGE.ID = CAB_ORDER_REPLY.MESSAGE_ID "
				+ "WHERE CAB_ORDER.ID = CAB_ORDER_REPLY.CAB_ORDER_ID ) AS ORDER_REPLY_MESSAGE "
				+ "FROM CAB_ORDER "
				+ "JOIN CUSTOMER ON CUSTOMER.ID = CAB_ORDER.CUSTOMER_ID "
				+ "JOIN COMPANY ON COMPANY.ID = CAB_ORDER.COMPANY_ID "
				+ "JOIN CAB_ORDER_STATE ON CAB_ORDER_STATE.ID = CAB_ORDER.STATE_ID "
				+ "LEFT JOIN CAB_ORDER_REPLY ON CAB_ORDER.ID = CAB_ORDER_REPLY.CAB_ORDER_ID " 
				+ "WHERE CUSTOMER.ID = ? "
				+ "ORDER BY CAB_ORDER.ID";
		
		String sqlGetOrderedCars = "SELECT " +
				"CAR.TYPE, " +
				"CAB_ORDER_HAS_CARS.QUANTITY, " +
				"CAB_ORDER_HAS_CARS.CAB_ORDER_ID " +
				"FROM CAB_ORDER_HAS_CARS " +
				"JOIN CAR ON CAB_ORDER_HAS_CARS.CAR_ID = CAR.ID " +
				"WHERE CAB_ORDER_HAS_CARS.CAB_ORDER_ID = ?";
		
		Object[] orderArgs = new Object[] { customerId };	// put the arguments for the query
		
		ArrayList<Order> orders = (ArrayList<Order>) this.jdbcTemplate.query(sqlGetOrder, orderArgs, new OrderRowMapper());	// query the db for all the orders for this customer
		
		// go through each order returned from the db
		for (int i = 0; i < orders.size(); i++)
		{
			Object[] carsArgs = new Object[] { orders.get(i).getId() };	// put the id of the first order in the arguments array
			ArrayList<OrderedCar> orderSpecificCars = (ArrayList<OrderedCar>) this.jdbcTemplate.query(sqlGetOrderedCars, carsArgs, new OrderedCarRowMapper());	// get the list of specific cars for each order
			
			orders.get(i).setCars(orderSpecificCars);	// add the list of order specific cars to each order.
		}
		
		// finally return all the orders 
		return orders;
	}

	@Override
	public ArrayList<Order> readAll() {

		String sqlGetOrder = " SELECT CAB_ORDER.ID, "
				+ "CUSTOMER.NAME AS CUSTOMER_NAME, "
				+ "COMPANY.NAME AS COMPANY_NAME, "
				+ "CAB_ORDER_STATE.NAME AS ORDER_STATE, "
				+ "CAB_ORDER.TIME_ORDERED, "
				+ "CAB_ORDER.TIME_TO_DELIVER AS REQUESTED_TIME_TO_DELIVER, "
				+ "CAB_ORDER.FROM_LOCATION, "
				+ "CAB_ORDER.TO_LOCATION, "
				+ "CAB_ORDER.CUSTOMER_NOTE, "
				+ "CAB_ORDER_REPLY.TIME_TO_DELIVER AS ORDER_REPLY_TIME_TO_DELIVER, "
				+ "CAB_ORDER_REPLY.TIME_OF_REPLY AS ORDER_REPLY_TIME, ( "
				+ "SELECT MESSAGE.MESSAGE	"
				+ "FROM CAB_ORDER_REPLY "
				+ "JOIN MESSAGE ON MESSAGE.ID = CAB_ORDER_REPLY.MESSAGE_ID "
				+ "WHERE CAB_ORDER.ID = CAB_ORDER_REPLY.CAB_ORDER_ID ) AS ORDER_REPLY_MESSAGE "
				+ "FROM CAB_ORDER "
				+ "JOIN CUSTOMER ON CUSTOMER.ID = CAB_ORDER.CUSTOMER_ID "
				+ "JOIN COMPANY ON COMPANY.ID = CAB_ORDER.COMPANY_ID "
				+ "JOIN CAB_ORDER_STATE ON CAB_ORDER_STATE.ID = CAB_ORDER.STATE_ID "
				+ "LEFT JOIN CAB_ORDER_REPLY ON CAB_ORDER.ID = CAB_ORDER_REPLY.CAB_ORDER_ID "
				+ "ORDER BY CAB_ORDER.ID";


		String sqlGetOrderedCars = "SELECT " +
				"CAR.TYPE, " +
				"CAB_ORDER_HAS_CARS.QUANTITY, " +
				"CAB_ORDER_HAS_CARS.CAB_ORDER_ID " +
				"FROM CAB_ORDER_HAS_CARS " +
				"JOIN CAR ON CAB_ORDER_HAS_CARS.CAR_ID = CAR.ID ";

		ArrayList<Order> orders = (ArrayList<Order>) this.jdbcTemplate.query(
				sqlGetOrder, new OrderRowMapper());	// query the db for all the orders
		
		ArrayList<OrderedCar> orderedCars = (ArrayList<OrderedCar>) this.jdbcTemplate
				.query(sqlGetOrderedCars, new OrderedCarRowMapper()); // query the db for all the ordered cars

		
		// go through all the orders and find their ordered cars
		for (int i = 0; i < orders.size(); i++) {
			long id = orders.get(i).getId();	// get the id of the ordered
			
			ArrayList<OrderedCar> orderSpecificCars = new ArrayList<OrderedCar>(); 	// list of all the cars for the selected order with index (i)
			
			// then find the cars and set it to the order
			for (int j = 0; j < orderedCars.size(); j++) {
				
				// check if the car matched the order
				if (id == orderedCars.get(j).getOrderId()) {
					orderSpecificCars.add(orderedCars.get(j)); // add the car to
																// the list of
																// order
																// specific cars
				}
			}
			orders.get(i).setCars(orderSpecificCars); // add the list of cars to the order
		}

		return orders;
	}

	@Override
	public void update(Order order) {
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Order order) {
		// TODO Auto-generated method stub

	}

}
