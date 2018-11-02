package TerranBot;
import java.util.List;
import java.util.Vector;
import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class TestBot1 extends DefaultBWListener {

    private Mirror mirror = new Mirror();

    private Game game;

    private Player self;
    
    //public List<BaseLocation> baseLocation;
    public List<BaseLocation> baseLocation;
    //number of SCV
    public int numSCV = 4;
    public boolean fstBR = false;
    public boolean supplyBuilder = false;
    public boolean brBuilder = false;
    public boolean bunkerBuilder = false;
    public boolean refineryBuilder = false;
    public int scoutID=9999;
    public int brBuilderID = 9998;
    public String enemyPlan = ""; 
    
    public Vector<Position> scoutingPos = new Vector<Position>();
    public boolean[] scouting = {false,false,false,false};
   
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }

    @Override
    public void onUnitCreate(Unit unit) {
        System.out.println("New unit " + unit.getType());
    }

    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();

        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        baseLocation = BWTA.getBaseLocations();
        //System.out.println(baseLocation.get(1).getPosition().toString() + "1111111111111");
        
        //Unit myBase = new Unit(999);
        
        System.out.println(self.getStartLocation().toPosition() + "My position !!!");
        
        for (int i = 0; i < baseLocation.size(); i++) {
        	// If this is a possible start location,
        	//System.out.println(baseLocation.size() + ":size");
        	System.out.println(baseLocation.get(i).getPosition().toString() + ":baseLocations");
        	if (baseLocation.get(i).isStartLocation()) {
        		// do something. For example send some unit to attack that position:
        		System.out.println(baseLocation.get(i).getPosition().toString() + ":StartLocation");
        		//If it is not our start position, then add it to scouting list.
        		if (Math.abs((baseLocation.get(i).getPosition().getX() - self.getStartLocation().toPosition().getX()))>100) {
        			scoutingPos.addElement(baseLocation.get(i).getPosition());
        			System.out.println("Adding a target location !!!");
        		}
        	}
        }
        
        int i = 0;
        for(BaseLocation baseLocation : BWTA.getBaseLocations()){
        	System.out.println("Base location #" + (++i) +". Printing location's region polygon:");
        	for(Position position: baseLocation.getRegion().getPolygon().getPoints()){
        		System.out.print(position + ", ");
        	}
        	System.out.println();
        }

    }

    @Override
    public void onFrame() {
        //game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());

        StringBuilder units = new StringBuilder("My units:\n");

        //iterate through my units
        for (Unit myUnit : self.getUnits()) {
            units.append(myUnit.getType()).append(" ").append(myUnit.getTilePosition()).append("\n");

            //if there's enough minerals, train an SCV
            if (myUnit.getType() == UnitType.Terran_Command_Center && self.minerals() >= 50 && myUnit.isIdle() && (self.supplyTotal() - self.supplyUsed() >= 2) && (numSCV < 20)) {
                myUnit.train(UnitType.Terran_SCV);
                System.out.println("Total supply: " + self.supplyTotal());
                System.out.println("Used supply: " + self.supplyUsed());
                numSCV += 1;
            }
            
            if (myUnit.getType() == UnitType.Terran_Barracks && self.minerals() >= 150 && myUnit.isIdle() && (self.supplyTotal() - self.supplyUsed() >= 2)) {
                myUnit.train(UnitType.Terran_Marine);
                System.out.println("Total supply: " + self.supplyTotal());
                System.out.println("Used supply: " + self.supplyUsed());
            }
            
          //if we're running out of supply and have enough minerals ...
            if ((self.supplyTotal() - self.supplyUsed() <= 4) && (self.minerals() >= 142) && (game.getFrameCount()%48 == 12)) {
            	//iterate over units to find a worker
        		if ((myUnit.getType() == UnitType.Terran_SCV) && (myUnit.getHitPoints() == 60) && (myUnit.isGatheringMinerals()) && (supplyBuilder == false) && (myUnit.canBuild())) {
        			//get a nice place to build a supply depot
        			//myUnit.holdPosition();
        			
        			TilePosition buildTile =
        				getBuildTile(myUnit, UnitType.Terran_Supply_Depot, self.getStartLocation());
        			//and, if found, send the worker to build it (and leave others alone - break;)
        			if (buildTile != null) {
        				myUnit.holdPosition();
        				myUnit.build(UnitType.Terran_Supply_Depot, buildTile);
        				System.out.println(myUnit.getID() + "build depot");
        				//supplyBuilder = true;
        				break;
        			}
        		}
            }                 
            
            //Build the first Br
            if ((numSCV >= 10) && (self.minerals() >= 200) && (fstBR == false) && (game.getFrameCount()%48 == 24)) {
            	//iterate over units to find a worker
        		if ((myUnit.getType() == UnitType.Terran_SCV) && (myUnit.getHitPoints() == 60) && (myUnit.isGatheringMinerals()) && (brBuilder == false) && (myUnit.canBuild())) {
        			//get a nice place to build a supply depot
        			//myUnit.holdPosition();
        			//brBuilder = true;
        			brBuilderID = myUnit.getID();
        			TilePosition buildTile =
        				getBuildTile(myUnit, UnitType.Terran_Barracks, self.getStartLocation());
        			//and, if found, send the worker to build it (and leave others alone - break;)
        			if (buildTile != null) {
        				myUnit.build(UnitType.Terran_Barracks, buildTile);
        				System.out.println(myUnit.getID() + "build br");
        				//fstBR = true;
        				break;
        			}
        		}
            }
            
            //Build bunker if enemyPlan is Zealot Rush
            if ((enemyPlan == "Zealot Rush") && (self.minerals() >= 200) && (game.getFrameCount()%48 == 24)) {
            	//iterate over units to find a worker
        		if ((myUnit.getType() == UnitType.Terran_SCV) && (myUnit.getHitPoints() == 60) && (myUnit.isGatheringMinerals()) && (bunkerBuilder == false) && (myUnit.canBuild())) {
        			//get a nice place to build a supply depot
        			//myUnit.holdPosition();
        			//bunkerBuilder = true;
        			TilePosition buildTile =
        				getBuildTile(myUnit, UnitType.Terran_Bunker, self.getStartLocation());
        			//and, if found, send the worker to build it (and leave others alone - break;)
        			if (buildTile != null) {
        				myUnit.build(UnitType.Terran_Bunker, buildTile);
        				System.out.println(myUnit.getID() + "build bunker");
        				//fstBR = true;
        				break;
        			}
        		}
            }
            /*
            if ((enemyPlan == "Zealot Rush") && (self.minerals() >= 300) && (game.getFrameCount()%48 == 8)) {
            	//iterate over units to find a worker
        		if ((myUnit.getType() == UnitType.Terran_SCV) && (myUnit.getHitPoints() == 60) && (myUnit.isGatheringMinerals()) && (bunkerBuilder == true) && (myUnit.canBuild())) {
        			//get a nice place to build a supply depot
        			//myUnit.holdPosition();
        			brBuilder = true;
        			TilePosition buildTile =
        				getBuildTile(myUnit, UnitType.Terran_Barracks, self.getStartLocation());
        			//and, if found, send the worker to build it (and leave others alone - break;)
        			if (buildTile != null) {
        				myUnit.build(UnitType.Terran_Barracks, buildTile);
        				System.out.println(myUnit.getID() + "build br");
        				//fstBR = true;
        				break;
        			}
        		}
            }
            */
          //Build refinery if enemyPlan is Zealot Rush
            if ((enemyPlan == "Dragoon") && (self.minerals() >= 200) && (game.getFrameCount()%48 == 24)) {
            	//iterate over units to find a worker
        		if ((myUnit.getType() == UnitType.Terran_SCV) && (myUnit.getHitPoints() == 60) && (myUnit.isGatheringMinerals()) && (refineryBuilder == false) && (myUnit.canBuild())) {
        			//get a nice place to build a supply depot
        			//myUnit.holdPosition();
        			refineryBuilder = true;
        			TilePosition buildTile =
        				getBuildTile(myUnit, UnitType.Terran_Refinery, self.getStartLocation());
        			//and, if found, send the worker to build it (and leave others alone - break;)
        			if (buildTile != null) {
        				myUnit.build(UnitType.Terran_Refinery, buildTile);
        				System.out.println(myUnit.getID() + "build refinery");
        				//fstBR = true;
        				break;
        			}
        		}
            }
            
            if (myUnit.getType() == UnitType.Terran_Barracks) {
            	fstBR = true;
            	brBuilder = false;
            	brBuilderID = 9998;
            }
            
            
            if ((myUnit.getType() == UnitType.Terran_Supply_Depot) && myUnit.isBeingConstructed()) {
            	supplyBuilder = true;
            }
            
            if (self.supplyTotal() - self.supplyUsed() >= 8) {
            	supplyBuilder = false;           	
            	} 
            
            if ((myUnit.getType() == UnitType.Terran_Bunker) && myUnit.isBeingConstructed()) {
            	bunkerBuilder = true;
            }
            //if ((myUnit.getType() == UnitType.Terran_Supply_Depot) && (myUnit.isBeingConstructed())) supplyBuilder = true;
            
            //if it is the 12th SCV, then send it to scouting
            if ((numSCV >= 12) && (scouting[0] == false) && (game.getFrameCount()%48 == 36)) {
            	if ((myUnit.getType() == UnitType.Terran_SCV) && (myUnit.getHitPoints() == 60) && myUnit.isGatheringMinerals() && myUnit.canMove()) {
            		scoutID = myUnit.getID();
            		myUnit.move(scoutingPos.get(0));
            		System.out.println(myUnit.getID() + scoutingPos.get(0).toString() + "scouting to !!!!!");
            		//System.out.println(scoutingPos.get(0) + "scouting to !!!!!");
            		scouting[0] = true;
            		myUnit.move(scoutingPos.get(0));
            		break;
            	}
            }          
            
            //If the scout is attacked, then move back to its base.
            if((myUnit.getType() == UnitType.Terran_SCV) && myUnit.isUnderAttack()) {            	
            	myUnit.move(self.getStartLocation().toPosition());
            	//System.out.println(myUnit.getHitPoints());
            	//System.out.println("Gateway number: " + getEnemyGatewayNum());
            	//System.out.println("Assimilator number: " + getEnemyAssimilatorNum());
            	//System.out.println("PCC number: " + getEnemyPCCNum());
            }                     
            //if (myUnit.isIdle() && (myUnit.getType() == UnitType.Terran_SCV) && (self.getUnits().get(1).getTrainingQueue().get(0)) System.out.println(myUnit.getID()+"is idle!!!");
            //if (myUnit.getType() == UnitType.Terran_Command_Center) myBase = myUnit;
            //if it's a drone and it's idle, send it to the closest mineral patch
            if (myUnit.getType().isWorker() && myUnit.isIdle() && (game.getFrameCount()%48 == 0) && (myUnit.getID() != scoutID) && (myUnit.getID() != brBuilderID) && (!myUnit.isMoving())) {
                Unit closestMineral = null;

                //find the closest mineral
                for (Unit neutralUnit : game.neutral().getUnits()) {
                    if (neutralUnit.getType().isMineralField()) {
                        if (closestMineral == null || myUnit.getDistance(neutralUnit) < myUnit.getDistance(closestMineral)) {
                            closestMineral = neutralUnit;
                        }
                    }
                }

                //if a mineral patch was found, send the drone to gather it
                if ((closestMineral != null) && (myUnit.getID() != scoutID)) {
                	//System.out.println(myUnit.getID()+ "Gathering");
                    myUnit.gather(closestMineral, false);
                    //System.out.println(myUnit.getID()+"gathering mineral!!");
                }
            }
            
            if (game.getFrameCount()%120 == 0) {
            	
            }
        }//For units ends
        
        //print enemy buildings
        if (game.getFrameCount()%120 == 0) {
        	System.out.println("Now it is frame: " + game.getFrameCount());
        	for (Unit u : game.enemy().getUnits()) {
            	//if this unit is in fact a building
            	if (u.getType().isBuilding()) {
            		System.out.println("enemy building:" + u.getType().toString());
            		//System.out.println(game.enemy().getUnits().size());
            	}
            }
        }       
        
        //Check if it is the first scouting. If so, then pass scouting result to decision tree and get result.
        if ((game.getFrameCount()%48 == 0) && (getEnemyBuildingsNum() >= 5) && (enemyPlan == "")) {
        	try {
				enemyPlan = DicisionTree.testTree(String.valueOf(getEnemyAssimilatorNum()),String.valueOf(getEnemyPCCNum()),String.valueOf(getEnemyGatewayNum())).toString();
				System.out.println("Assimilator:"+getEnemyAssimilatorNum());
				System.out.println("PCC:"+getEnemyPCCNum());
				System.out.println("Gateway:"+getEnemyGatewayNum());
				System.out.println(enemyPlan);
				scoutID = 9999;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
        
        //Check if enemyPlan is known
        /*if (enemyPlan == "Zealot Rush") {        	
        	System.out.println("Zealot Rush!!!!");
        	enemyPlan = "0";
        } else if (enemyPlan == "Dragoon") {
        	System.out.println("Dragoon");
        	enemyPlan = "0";
        }*/
        /*if (game.enemy().getUnits().size() != 0) {
        	System.out.println(game.enemy().getUnits().size());
        }*/
        
        //draw my units on screen
        game.drawTextScreen(10, 25, units.toString());
    }//onFrame() end
    
    public int getEnemyBuildingsNum() {
    	int num = 0;
    	for (Unit u : game.enemy().getUnits()) {
        	//if this unit is in fact a building
        	if (u.getType().isBuilding()) {
        		num+=1;
        	}
        }
    	return num;
    }
    
    public int getEnemyGatewayNum() {
    	int num = 0;
    	for (Unit u : game.enemy().getUnits()) {
        	//if this unit is in fact a building
        	if (u.getType() == UnitType.Protoss_Gateway) {
        		num+=1;
        	}
        }
    	return num;
    }
    
    public int getEnemyAssimilatorNum() {
    	int num = 0;
    	for (Unit u : game.enemy().getUnits()) {
        	//if this unit is in fact a building
        	if (u.getType() == UnitType.Protoss_Assimilator) {
        		num+=1;
        	}
        }
    	return num;
    }
    
    public int getEnemyPCCNum() {
    	int num = 0;
    	for (Unit u : game.enemy().getUnits()) {
        	//if this unit is in fact a building
        	if (u.getType() == UnitType.Protoss_Cybernetics_Core) {
        		num+=1;
        	}
        }
    	return num;
    }
	 // Returns a suitable TilePosition to build a given building type near
	 // specified TilePosition aroundTile, or null if not found. (builder parameter is our worker)
	public TilePosition getBuildTile(Unit builder, UnitType buildingType, TilePosition aroundTile) {
	 	TilePosition ret = null;
	 	int maxDist = 3;
	 	int stopDist = 40;
	
	 	// Refinery, Assimilator, Extractor
	 	if (buildingType.isRefinery()) {
	 		for (Unit n : game.neutral().getUnits()) {
	 			if ((n.getType() == UnitType.Resource_Vespene_Geyser) &&
	 					( Math.abs(n.getTilePosition().getX() - aroundTile.getX()) < stopDist ) &&
	 					( Math.abs(n.getTilePosition().getY() - aroundTile.getY()) < stopDist )
	 					) return n.getTilePosition();
	 		}
	 	}
	
	 	while ((maxDist < stopDist) && (ret == null)) {
	 		for (int i=aroundTile.getX()-maxDist; i<=aroundTile.getX()+maxDist; i++) {
	 			for (int j=aroundTile.getY()-maxDist; j<=aroundTile.getY()+maxDist; j++) {
	 				if (game.canBuildHere(new TilePosition(i,j), buildingType, builder, false)) {
	 					// units that are blocking the tile
	 					boolean unitsInWay = false;
	 					for (Unit u : game.getAllUnits()) {
	 						if (u.getID() == builder.getID()) continue;
	 						if ((Math.abs(u.getTilePosition().getX()-i) < 4) && (Math.abs(u.getTilePosition().getY()-j) < 4)) unitsInWay = true;
	 					}
	 					if (!unitsInWay) {
	 						return new TilePosition(i, j);
	 					}
	 					// creep for Zerg
	 					if (buildingType.requiresCreep()) {
	 						boolean creepMissing = false;
	 						for (int k=i; k<=i+buildingType.tileWidth(); k++) {
	 							for (int l=j; l<=j+buildingType.tileHeight(); l++) {
	 								if (!game.hasCreep(k, l)) creepMissing = true;
	 								break;
	 							}
	 						}
	 						if (creepMissing) continue;
	 					}
	 				}
	 			}
	 		}
	 		maxDist += 2;
	 	}
	
	 	if (ret == null) game.printf("Unable to find suitable build position for "+buildingType.toString());
	 	return ret;
	}

	public static void main(String[] args) throws Exception {
		//System.out.println(DicisionTree.testTree());
		//System.out.println(DicisionTree.testTree("0","0","2"));
        new TestBot1().run();
    }
}