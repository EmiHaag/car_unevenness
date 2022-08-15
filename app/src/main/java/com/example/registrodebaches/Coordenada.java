package com.example.registrodebaches;



public class Coordenada {
    private double lat,lng;
    private int coordenadaId;
    public  Coordenada(int coordenadaId, Double lat, Double lng){

        this.lat = lat;
        this.lng = lng;
        this.coordenadaId = coordenadaId;
    }

    public Double getLat(){
        return this.lat;
    }

    public Double getLng(){
        return this.lng;
    }

    public int getId(){
        return this.coordenadaId;
    }
}
