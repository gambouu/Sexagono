/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.upc.epsevg.prop.hex;

import edu.upc.epsevg.prop.hex.HexGameStatus;
import edu.upc.epsevg.prop.hex.PlayerType;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus2;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus3;
import edu.upc.epsevg.prop.hex.players.ProfeGameStatus3.Result;
import edu.upc.epsevg.prop.hex.players.Sexagono;
/**
 *
 * @author bernat
 */
public class UnitTesting {
    
    public static void main(String[] args) {
    
        byte[][] board0 = {
        //X   0  1  2  3  4  5  6  7  8
            { 0, 0, 0, 0, 0, 0, 0, 0, 0},                     // 0   Y
             { 0, 0, 0, 0, 0, 0, 0, 0, 0},                    // 1
               { 0, 0, 0, 0, 0, 0, 0, 0, 0},                  // 2
                 { 0, 0, 0, 0, 0, 0, 0, 1, 0},                // 3
                   { 0, 0, 0, 0, 0, 0, 0, 0, 0},              // 4  
                     { 0, 0, 0, 0, 0, 0, 0, 0, 0},            // 5    
                       { 0, 0, 0, 0, 0, 0, 0, 0, 0},          // 6      
                         { 0, 0, 0, 0, 0, 0, 0, 0, 0},        // 7       
                           { 0, 0, 0, 0, 0, 0, 0, 0, 0}       // 8    Y         
        };
        byte[][] board1 = {
        //X   0  1  2  3  4  5  6  7  8
            { 0, 0, 0, -1, 0, 0, 0, 0, 0},                     // 0   Y
             { 0, 0, 0, -1, 0, 0, 0, 0, 0},                    // 1
               { 0, 0, 0, -1, 0, 0, 0, 0, 0},                  // 2
                 { 0, 0, 0, -1, 0, 0, 0, 0, 0},                // 3
                   { 0, 0, 0, -1, 0, 0, 0, 0, 0},              // 4  
                     { 0, -1, 0, 0, 0, 0, 0, 0, 0},            // 5    
                       { 0, -1, 0, 0, 0, 0, 0, 0, 0},          // 6      
                         { 0, -1, 0, -1, 0, 0, 0, -1, 0},        // 7       
                           { 0, -1, 0, 0, 0, 0, 0, 0, 0}       // 8    Y         
        };

        HexGameStatus gs = new HexGameStatus(board0, PlayerType.PLAYER1);        
        int result = Sexagono.dijkstra(gs, PlayerType.PLAYER1);
        System.out.println(result);
    }
    
}
