package com.route.sujoy.routenotifications;

import java.util.ArrayList;

/**
 * Created by Sujoy on 07-06-2015.
 */
class RouteObject {
    private String routeName = "";
    private String routeDesc = "";
    private String routeCity = "";
    private String routeState = "";
    private String routeCountry = "";
    public ArrayList<RoutesUtils.Coordinates> busLocationsArray  = new ArrayList<RoutesUtils.Coordinates>();
    public ArrayList<RouteListActivity.RouteStopsObject> busStopsArray  = new ArrayList<RouteListActivity.RouteStopsObject>();
    private String UUID = "";

    public RouteObject(String routeName) {
        this.routeName = routeName;
    }

    public RouteObject() {

    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public String getRouteDesc() {
        return routeDesc;
    }

    public void setRouteDesc(String routeDesc) {
        this.routeDesc = routeDesc;
    }

    public String getRouteCity() {
        return routeCity;
    }

    public void setRouteCity(String routeCity) {
        this.routeCity = routeCity;
    }

    public String getRouteState() {
        return routeState;
    }

    public void setRouteState(String routeState) {
        this.routeState = routeState;
    }

    public String getRouteCountry() {
        return routeCountry;
    }

    public void setRouteCountry(String routeCountry) {
        this.routeCountry = routeCountry;
    }

    public ArrayList<RoutesUtils.Coordinates> getBusLocationsArray() {
        return busLocationsArray;
    }

    public void setBusLocationsArray(ArrayList<RoutesUtils.Coordinates> busLocationsArray) {
        this.busLocationsArray = busLocationsArray;
    }

    public ArrayList<RouteListActivity.RouteStopsObject> getBusStopsArray() {
        return busStopsArray;
    }

    public void setBusStopsArray(ArrayList<RouteListActivity.RouteStopsObject> busStopsArray) {
        this.busStopsArray = busStopsArray;
    }


    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getUUID() {
        return UUID;
    }
}


