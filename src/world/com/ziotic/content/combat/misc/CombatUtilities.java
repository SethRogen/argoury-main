package com.ziotic.content.combat.misc;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import com.ziotic.Constants.Equipment;
import com.ziotic.Static;
import com.ziotic.content.combat.Combat.ActionType;
import com.ziotic.engine.tick.Tick;
import com.ziotic.logic.Entity;
import com.ziotic.logic.item.EquipmentDefinition.Bonuses;
import com.ziotic.logic.item.PossesedItem;
import com.ziotic.logic.map.Coverage;
import com.ziotic.logic.map.Directions;
import com.ziotic.logic.map.Directions.NormalDirection;
import com.ziotic.logic.map.Region;
import com.ziotic.logic.map.Tile;
import com.ziotic.logic.object.GameObject;
import com.ziotic.logic.player.Player;
import com.ziotic.utility.Logging;

public class CombatUtilities {

    private static final Random RANDOM = new Random();
	@SuppressWarnings("unused")
	private static final Logger LOGGER = Logging.log();

    private static final String[][] KILL_MESSAGES = new String[][]{
            {"You were clearly a better fighter than ", " ."},
            {"", " was finished with an astonishing blow."},
            {"", " regrets the day he met you in combat."},
            {"", " was no match for you."},
            {"", " falls before your might."},
            {"With a crushing blow you finish ", " ."},
            {"You have sent ", " to their grave."},
            {"With an almighty strike ", " was sent to heaven."},
            {"It seems nobody can defeat you, certainly not ", " ."}
    };

    public static final String getKillMessage(Player player) {
        String[] message = KILL_MESSAGES[RANDOM.nextInt(KILL_MESSAGES.length)];
        return message[0] + player.getName() + message[1];
    }

    public static boolean moveToCorrectPosition(Entity entity, Entity partner, ActionType type, boolean combat, boolean npcCheck) {
    	try {
        entity.faceEntity(partner);
        Tile loc = entity.getLocation();
        int dx = loc.getX() - partner.getLocation().getX();
        int dy = loc.getY() - partner.getLocation().getY(); //eCenter.getY() - pCenter.getY();
        int distance = entity.getCoverage().center().distance(partner.getCoverage().center()); 
        boolean succes = false;
        int x = entity.getLocation().getX();
        int y = entity.getLocation().getY();
        int counter;
        if (entity instanceof Player) {
        	Player player = (Player) entity;
        	if (partner instanceof Player)
            	counter = player.isRunning() ? ((Player) partner).isRunning() ? 2 : 1 : 1;
            else 
            	counter = player.isRunning() ? 2 : 1; 
        } else {
        	counter = 2;
        }
        for (int i = 0; i < counter; i++) {
        	succes = false;
            loc = Tile.locate(x, y, entity.getZ());
            NextNode next = getNextNode(loc, dx, dy, distance, true, entity, partner, type, npcCheck);
            if (next == null) {
                break;
            }
            if (next.tile == null) {
                break;
            }
            if (next.canMove) {
                if (partner.getCoverage().within(next.tile)) {
                	succes = true;
                	continue;
                }
                x = next.tile.getX();
                y = next.tile.getY();
                dx = x - partner.getLocation().getX(); 
                dy = y - partner.getLocation().getY(); 
            	succes = true;
                entity.updateCoverage(next.tile);
                entity.getPathProcessor().add(next.tile);
            } else {
                // TODO handle being stucked!
                break;
            }
        }
        return succes;
    	} catch (Exception e) {
    		e.printStackTrace();
    		return false;
    	}
    }

    protected static NextNode getNextNode(Tile loc, int dx, int dy, int distance, boolean combat, Entity entity, Entity partner, ActionType type, boolean npcCheck) {
        NormalDirection direction = null;
        if (combat) {
        	if (entity.getCoverage().correctCombatPosition(entity, partner, partner.getCoverage(), type, getAttackDistance(entity, type))) {
        		return null;
        	}
        } else {
        	if (entity.getCoverage().correctFinalFollowPosition(partner.getCoverage())) {
        		return null;
        	}
        }
        if (entity.getSize() > 1) {
    		Tile eCenter = entity.getCoverage().center();
    		Tile pCenter = partner.getCoverage().center();
        	if (entity.getCoverage().intersect(partner.getCoverage())) {
        		if (eCenter == pCenter) {
        			if (loc.canMove(NormalDirection.SOUTH_WEST, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.SOUTH_WEST;
    				} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.WEST;
    				} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.SOUTH;
    				} else if (loc.canMove(NormalDirection.NORTH_WEST, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.NORTH_WEST;
    				} else if (loc.canMove(NormalDirection.NORTH_EAST, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.NORTH_EAST;
    				} else if (loc.canMove(NormalDirection.SOUTH_EAST, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.SOUTH_EAST;
    				} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.EAST;
    				} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
    					direction = NormalDirection.NORTH;
    				} 
        		} else if (eCenter.right(pCenter)) {
        			if (eCenter.above(pCenter)) {
        				if (loc.canMove(NormalDirection.NORTH_EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH_EAST;
        				} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.EAST;
        				} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH;
        				}
        			} else if (pCenter.under(pCenter)) {
        				if (loc.canMove(NormalDirection.SOUTH_EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH_EAST;
        				} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.EAST;
        				} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH;
        				}
        			} else {
        				if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.EAST;
        				} else if (loc.canMove(NormalDirection.NORTH_EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH_EAST;
        				} else if (loc.canMove(NormalDirection.SOUTH_EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH_EAST;
        				} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH;
        				} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH;
        				}
        			}
        		} else if (eCenter.left(pCenter)) {
        			if (eCenter.above(pCenter)) {
        				if (loc.canMove(NormalDirection.NORTH_WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH_WEST;
        				} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.WEST;
        				} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH;
        				}
        			} else if (pCenter.under(pCenter)) {
        				if (loc.canMove(NormalDirection.SOUTH_WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH_WEST;
        				} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.WEST;
        				} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH;
        				}
        			} else {
        				if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.WEST;
        				} else if (loc.canMove(NormalDirection.NORTH_WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH_WEST;
        				} else if (loc.canMove(NormalDirection.SOUTH_WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH_WEST;
        				} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH;
        				} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH;
        				}
        			}
        		} else {
        			if (eCenter.above(pCenter)) {
        				if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH;
        				} else if (loc.canMove(NormalDirection.NORTH_EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH_EAST;
        				} else if (loc.canMove(NormalDirection.NORTH_WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH_WEST;
        				} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.EAST;
        				} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.WEST;
        				}
        			} else if (eCenter.under(pCenter)) {
        				if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH;
        				} else if (loc.canMove(NormalDirection.SOUTH_EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH_EAST;
        				} else if (loc.canMove(NormalDirection.SOUTH_WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH_WEST;
        				} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.EAST;
        				} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.WEST;
        				}
        			}
        		}
        	} else {
        		Coverage eC = entity.getCoverage();
        		Coverage pC = partner.getCoverage();
        		int absDX = Math.abs(dx);
        		int absDY = Math.abs(dy);
        		if (eC.right(pC)) {
        			if (eC.above(pC)) {
        				if (loc.canMove(NormalDirection.SOUTH_WEST, entity.getSize(), npcCheck)) {
        					if (absDY <= 1 && absDY >= 0) {
        						if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						}
        					} else if (absDX <= 1 && absDX >= 0) {
        						if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						}
        					} else {
        						direction = NormalDirection.SOUTH_WEST;
        					}
        				} else {
        					if (dx > dy) {
        						if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						}
        					} else if (dx < dy) {
        						if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						}
        					} else {
        						if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						}
        					}
        				}
        			} else if (eC.under(pC)) {
        				if (loc.canMove(NormalDirection.NORTH_WEST, entity.getSize(), npcCheck)) {
        					if (absDY <= 1 && absDY >= 0) {
        						if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						}
        					} else if (absDX <= 1 && absDX >= 0) {
        						if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						}
        					} else {
        						direction = NormalDirection.NORTH_WEST;
        					}
        				} else {
        					if (dx > -dy) {
        						if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						}
        					} else if (dx < -dy) {
        						if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						}
        					} else {
        						if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						} else if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.WEST;
        						}
        					}
        				}
        			} else {
        				if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.WEST;
        				}
        			}
        		} else if (eC.left(pC)) {
        			if (eC.above(pC)) {
        				if (loc.canMove(NormalDirection.SOUTH_EAST, entity.getSize(), npcCheck)) {
        					if (absDY <= 1 && absDY >= 0) {
        						if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						}
        					} else if (absDX <= 1 && absDX >= 0) {
        						if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						}
        					} else {
        						direction = NormalDirection.SOUTH_EAST;
        					}
        				} else {
        					if (-dx > dy) {
        						if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						}
        					} else if (-dx < dy) {
        						if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						}
        					} else {
        						if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.SOUTH;
        						} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						}
        					}
        				}
        			} else if (eC.under(pC)) {
        				if (loc.canMove(NormalDirection.NORTH_EAST, entity.getSize(), npcCheck)) {
        					if (absDY <= 1 && absDY >= 0) {
        						if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						}
        					} else if (absDX <= 1 && absDX >= 0) {
        						if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						}
        					} else {
        						direction = NormalDirection.NORTH_EAST;
        					}
        				} else {
        					if (-dx > -dy) {
        						if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						}
        					} else if (-dx < -dy) {
        						if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						}
        					} else {
        						if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.NORTH;
        						} else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
                					direction = NormalDirection.EAST;
        						}
        					}
        				}
        			} else {
        				if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.EAST;
        				}
        			}
        		} else {
        			if (eC.above(pC)) {
        				if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.SOUTH;
        				}
        			} else if (eC.under(pC)) {
        				if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
        					direction = NormalDirection.NORTH;
        				}
        			}
        		}
        	}
        	if (direction == null) {
        		return null;
        	}
        	return new NextNode(loc, direction, loc.canMove(direction, entity.getSize(), npcCheck));
        } else {
	        if (dx > 0) {
	            if (dy > 0) {
	                if (dx == 1 && dy == 1 && combat) {
	                    if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.WEST;
	                    } else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.SOUTH;
	                    } else {
	                        direction = NormalDirection.WEST; // random w/e
	                    }
	                } else {
	                    if (loc.canMove(NormalDirection.SOUTH_WEST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.SOUTH_WEST;
	                    } else {
	                        if (dy > dx) {
	                            if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.WEST;
	                            } else {
	                                direction = NormalDirection.SOUTH;
	                            }
	                        } else if (dy < dx) {
	                            if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.SOUTH;
	                            } else {
	                                direction = NormalDirection.WEST;
	                            }
	                        } else {
	                        	if (loc.canMove(NormalDirection.SOUTH_WEST, entity.getSize(), npcCheck)) {
	                        		direction = NormalDirection.SOUTH_WEST;
	                        	} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.SOUTH;
	                            } else {
	                                direction = NormalDirection.WEST;
	                            }
	                        }
	                    }
	                }
	            } else if (dy < 0) {
	                if (dx == 1 && dy == -1 && combat) {
	                    if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.WEST;
	                    } else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.NORTH;
	                    } else {
	                        direction = NormalDirection.WEST; // random w/e
	                    }
	                } else {
	                    if (loc.canMove(NormalDirection.NORTH_WEST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.NORTH_WEST;
	                    } else {
	                        if (Math.abs(dy) > Math.abs(dx)) {
	                            if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.WEST;
	                            } else {
	                                direction = NormalDirection.NORTH;
	                            }
	                        } else if (Math.abs(dy) < Math.abs(dx)) {
	                            if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.NORTH;
	                            } else {
	                                direction = NormalDirection.WEST;
	                            }
	                        } else {
	                        	if (loc.canMove(NormalDirection.NORTH_WEST, entity.getSize(), npcCheck)) {
	                        		direction = NormalDirection.NORTH_WEST;
	                        	} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.NORTH;
	                            } else {
	                                direction = NormalDirection.WEST;
	                            }
	                        }
	                    }
	                }
	            } else {
	                direction = NormalDirection.WEST;
	            }
	        } else if (dx < 0) {
	            if (dy > 0) {
	                if (dx == -1 && dy == 1 && combat) {
	                    if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.EAST;
	                    } else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.SOUTH;
	                    } else {
	                        direction = NormalDirection.EAST; // random w/e
	                    }
	                } else {
	                    if (loc.canMove(NormalDirection.SOUTH_EAST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.SOUTH_EAST;
	                    } else {
	                        if (Math.abs(dy) > Math.abs(dx)) {
	                            if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.EAST;
	                            } else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.SOUTH;
	                            }
	                        } else if (Math.abs(dy) < Math.abs(dx)) {
	                            if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.SOUTH;
	                            } else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.EAST;
	                            }
	                        } else {
	                        	if (loc.canMove(NormalDirection.SOUTH_EAST, entity.getSize(), npcCheck)) {
	                        		direction = NormalDirection.SOUTH_EAST;
	                        	} else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.SOUTH;
	                            } else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.EAST;
	                            }
	                        }
	                    }
	                }
	            } else if (dy < 0) {
	                if (dx == -1 && dy == -1 && combat) {
	                    if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.EAST;
	                    } else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.NORTH;
	                    } else {
	                        direction = NormalDirection.EAST; // random w/e
	                    }
	                } else {
	                    if (loc.canMove(NormalDirection.NORTH_EAST, entity.getSize(), npcCheck)) {
	                        direction = NormalDirection.NORTH_EAST;
	                    } else {
	                        if (Math.abs(dy) > Math.abs(dx)) {
	                            if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.EAST;
	                            } else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.NORTH;
	                            }
	                        } else if (Math.abs(dy) < Math.abs(dx)) {
	                            if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.NORTH;
	                            } else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.EAST;
	                            }
	                        } else {
	                        	if (loc.canMove(NormalDirection.NORTH_EAST, entity.getSize(), npcCheck)) {
	                        		direction = NormalDirection.NORTH_EAST;
	                        	} else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.NORTH;
	                            } else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                                direction = NormalDirection.EAST;
	                            }
	                        }
	                    }
	                }
	            } else {
	                direction = NormalDirection.EAST;
	            }
	        } else {
	            if (dy > 0) {
	                direction = NormalDirection.SOUTH;
	            } else if (dy < 0) {
	                direction = NormalDirection.NORTH;
	            } else {
	                if (loc.canMove(NormalDirection.WEST, entity.getSize(), npcCheck)) {
	                    direction = NormalDirection.WEST;
	                } else if (loc.canMove(NormalDirection.EAST, entity.getSize(), npcCheck)) {
	                    direction = NormalDirection.EAST;
	                } else if (loc.canMove(NormalDirection.NORTH, entity.getSize(), npcCheck)) {
	                    direction = NormalDirection.NORTH;
	                } else if (loc.canMove(NormalDirection.SOUTH, entity.getSize(), npcCheck)) {
	                    direction = NormalDirection.SOUTH;
	                } else {
	                    direction = NormalDirection.SOUTH; // random w/e
	                }
	
	            }
	        }
	        if (direction == null) {
	            return null;
	        }
	        return new NextNode(loc, direction, loc.canMove(direction, entity.getSize(), npcCheck));
        }
    }

    public static boolean inCorrectPosition(Entity entity, Entity partner, ActionType type) {
        int maxDistance = getAttackDistance(entity, type);
        return entity.getCoverage().correctCombatPosition(entity, partner, partner.getCoverage(), type, maxDistance);
    }

    public static boolean inCorrectFollowPosition(Entity attacker,
                                                  Entity victim, ActionType type) {
        int maxDistance = getAttackDistance(attacker, type);
        int distance = attacker.getLocation().distance(victim.getLocation());
        int addition = 1;
        if (attacker instanceof Player) {
            Player p = (Player) attacker;
            if (p.getCombat().isFrozen())
                addition = 0;
            else
                addition = p.isRunning() ? 3 : 1;
        }
        maxDistance += addition;
        if (distance < maxDistance) {
            return true;
        } else
            return false;
    }

    public static final int getAttackDistance(Entity entity, ActionType type) {
        if (entity instanceof Player) {
            Player player = (Player) entity;
            switch (type) {
                case MELEE:
                    return 1;
                case MAGIC:
                    return 8;
                case RANGED:
                    if (player.getCombat().weapon.style == Styles.STYLE_LONG_RANGE)
                        return 10;
                    else
                        return 8;
                default:
                    return 1;
            }
        } else {
        	switch (type) {
        	case MELEE:
                return 1;
            case MAGIC:
                return 8;
            case RANGED:
                return 8;
            default:
                return 1;
        	}
        }
    }

    public static boolean canAttackLevelBased(Entity entity, Entity victim, boolean sendMessage) {
        if (entity instanceof Player && victim instanceof Player) {
            Player player = (Player) entity;
            Player victim_ = (Player) victim;
            if (victim_.isInPVP()) {
                if (player.isInPVP()) {
                    int wildyLevelDifference = player.getLocation().wildernessLevel() - victim_.getLocation().wildernessLevel();
                    int combatDifference = Math.abs(player.getLevels().getCombatLevel() - victim_.getLevels().getCombatLevel());
                    if (victim_.getLocation().wildernessLevel() >= combatDifference) {
                        return true;
                    } else {
                        if (wildyLevelDifference < 0 && sendMessage)
                            Static.proto.sendMessage(player, "You need to move deeper into the wilderness to attack this player. ");
                        else if (sendMessage)
                            Static.proto.sendMessage(player, "Your combat level difference is too big to attack this player.");
                        return false;
                    }

                } else {
                    if (sendMessage)
                        Static.proto.sendMessage(player, "You must be in a PvP zone to attack this player.");
                    return false;
                }
            } else {
                if (sendMessage)
                    Static.proto.sendMessage(player, "This player must be in a PvP zone to be attacked.");
                return false;
            }
        }
        return true;
    }

    private static class NextNode {
        Tile tile = null;
        boolean canMove = false;

        public NextNode(Tile loc, NormalDirection dir, boolean canMove) {
            this.canMove = canMove;
            if (canMove) {
                tile = loc.translate(Directions.DIRECTION_DELTA_X[dir.intValue()], Directions.DIRECTION_DELTA_Y[dir.intValue()], 0);
            }
        }
    }

    public static class Weapon {

        public int index;
        public int type;
        public int style;

        public Weapon(int type, int style) {
            this.type = type;
            this.style = style;
        }

        public int getIndex() {
            return index;
        }

    }

    public static class SpecialEnergy {

        Player player;
        protected int amount = 100;
        protected int drained = 0;

        public SpecialEnergy(Player player) {
            this.player = player;
        }

        public int getConfigAmount() {
            int result = amount - drained;
            if (result > 100)
                result = 100;
            if (result < 0)
                result = 0;
            int dif = result % 5;
            return (result - dif) * 10;
        }

        public int getAmount() {
            return amount;
        }

        public void setAmount(int amount) {
            this.amount = amount;
        }

        public void setDrain(int amount) {
            this.drained = amount;
        }

        public void decrease(int amount) {
            this.amount -= amount;
            if (this.amount < 0)
                this.amount = 0;
            Tick tick = player.retrieveTick("RestoreSpecial");
            if (tick == null) {
                player.registerTick(new Tick("RestoreSpecial", 25) {
                    @Override
                    public boolean execute() {
                        player.getCombat().specialEnergy.increase(5);
                        if (player.getCombat().specialEnergy.amount >= 100)
                            return false;
                        return true;
                    }
                });
            } else if (!tick.running()) {
                player.registerTick(new Tick("RestoreSpecial", 25) {
                    @Override
                    public boolean execute() {
                        player.getCombat().specialEnergy.increase(5);
                        if (player.getCombat().specialEnergy.amount >= 100)
                            return false;
                        return true;
                    }
                });
            }
            update();
        }

        public void increaseDrain(int amount) {
            drained += amount;
            if (drained > 10)
                drained = 10;
            Tick tick = player.retrieveTick("NormalizeSpecialDrain");
            if (tick == null) {
                player.registerTick(new Tick("NormalizeSpecialDrain", 25) {
                    @Override
                    public boolean execute() {
                        player.getCombat().specialEnergy.normalizeDrain(5);
                        if (player.getCombat().specialEnergy.drained <= 0)
                            return false;
                        return true;
                    }
                });
            } else if (!tick.running()) {
                player.registerTick(new Tick("NormalizeSpecialDrain", 25) {
                    @Override
                    public boolean execute() {
                        player.getCombat().specialEnergy.normalizeDrain(5);
                        if (player.getCombat().specialEnergy.drained <= 0)
                            return false;
                        return true;
                    }
                });
            }
            update();
        }

        public void normalizeDrain(int amount) {
            drained -= amount;
            if (drained < 0)
                drained = 0;
            update();
        }

        public void increase(int amount) {
            this.amount += amount;
            if (this.amount > 100)
                this.amount = 100;
            update();
        }

        public void update() {
            Static.proto.sendConfig(player, 300, getConfigAmount());
        }

        public void onLogin() {
            if (amount < 100)
                player.registerTick(new Tick("RestoreSpecial", 25) {
                    @Override
                    public boolean execute() {
                        player.getCombat().specialEnergy.increase(5);
                        if (player.getCombat().specialEnergy.amount >= 100)
                            return false;
                        return true;
                    }
                });
            if (drained > 0)
                player.registerTick(new Tick("NormalizeSpecialDrain", 25) {
                    @Override
                    public boolean execute() {
                        player.getCombat().specialEnergy.normalizeDrain(5);
                        if (player.getCombat().specialEnergy.drained <= 0)
                            return false;
                        return true;
                    }
                });
        }
    }

    /**
     * Weapon interface style and type tracking utility.
     *
     * @author Maxime Meire
     */
    public static class Styles {

        /**
         * STYLES
         */
        public static final int STYLE_ACCURATE = 0;
        public static final int STYLE_AGGRESSIVE = 1;
        public static final int STYLE_CONTROLLED = 2;
        public static final int STYLE_DEFENSIVE = 3;
        public static final int STYLE_RAPID = 4;
        public static final int STYLE_LONG_RANGE = 5;

        /**
         * TYPES
         */
        public static final int TYPE_STAB = 0;
        public static final int TYPE_SLASH = 1;
        public static final int TYPE_CRUSH = 2;
        public static final int TYPE_RANGED = 3;
        public static final int TYPE_MAGIC = 4;

        private Styles() {
        }

        public static int getType(int groupId, int button) {
            if (styles.get(groupId).config.length < button + 1) {
                button--;
            }
            return styles.get(groupId).config[button][1];
        }

        public static int getStyle(int groupId, int button) {
            if (styles.get(groupId).config.length < button + 1) {
                button--;
            }
            return styles.get(groupId).config[button][0];
        }

        public static int setIndex(int groupId, Weapon weapon, int button) {
            if (styles.get(groupId).config.length < button + 1) {
                weapon.index -= 1;
            }
            return weapon.index;
        }
        
        /**
         * Only to be used by npc's
         * @param weapon
         * @param type
         */
        public static void setType(Weapon weapon, int type) {
        	weapon.type = type;
        }

        public static int getAttackBonusForType(Entity entity, int type) {
            switch (type) {
            case TYPE_STAB:
                return (int) entity.getBonuses()[Bonuses.OFFENSIVE_STAB];
            case TYPE_SLASH:
                return (int) entity.getBonuses()[Bonuses.OFFENSIVE_SLASH];
            case TYPE_CRUSH:
                return (int) entity.getBonuses()[Bonuses.OFFENSIVE_CRUSH];
            case TYPE_RANGED:
                return (int) entity.getBonuses()[Bonuses.OFFENSIVE_RANGED];
            case TYPE_MAGIC:
                return (int) entity.getBonuses()[Bonuses.OFFENSIVE_MAGIC];
            default:
                return 0;
            }
        }

        public static int getDefenceBonusForType(Entity entity, int type) {
            switch (type) {
                case TYPE_STAB:
                    return (int) entity.getBonuses()[Bonuses.DEFENSIVE_STAB];
                case TYPE_SLASH:
                    return (int) entity.getBonuses()[Bonuses.DEFENSIVE_SLASH];
                case TYPE_CRUSH:
                    return (int) entity.getBonuses()[Bonuses.DEFENSIVE_CRUSH];
                case TYPE_RANGED:
                    return (int) entity.getBonuses()[Bonuses.DEFENSIVE_RANGED];
                case TYPE_MAGIC:
                    return (int) entity.getBonuses()[Bonuses.DEFENSIVE_MAGIC];
                default:
                    return 0;
            }
        }

        private static final Map<Integer, Style> styles = new HashMap<Integer, Style>();

        private static Styles instance = new Styles();

        private class Style {
            int[][] config;

            private Style(int[][] config) {
                this.config = config;
            }
        }

        static {
            styles.put(0, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_CRUSH}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_CRUSH}}));
            styles.put(1, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_CRUSH}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_CRUSH}}));
            styles.put(2, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_SLASH}, {STYLE_AGGRESSIVE, TYPE_SLASH}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_SLASH}}));
            styles.put(3, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_CRUSH}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_CRUSH}}));
            styles.put(4, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_STAB}, {STYLE_AGGRESSIVE, TYPE_STAB}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_STAB}}));
            styles.put(5, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_STAB}, {STYLE_AGGRESSIVE, TYPE_STAB}, {STYLE_AGGRESSIVE, TYPE_SLASH}, {STYLE_DEFENSIVE, TYPE_STAB}}));
            styles.put(6, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_SLASH}, {STYLE_AGGRESSIVE, TYPE_SLASH}, {STYLE_CONTROLLED, TYPE_STAB}, {STYLE_DEFENSIVE, TYPE_SLASH}}));
            styles.put(7, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_SLASH}, {STYLE_AGGRESSIVE, TYPE_SLASH}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_SLASH}}));
            styles.put(8, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_CRUSH}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_CONTROLLED, TYPE_STAB}, {STYLE_DEFENSIVE, TYPE_CRUSH}}));
            styles.put(9, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_SLASH}, {STYLE_AGGRESSIVE, TYPE_SLASH}, {STYLE_CONTROLLED, TYPE_STAB}, {STYLE_DEFENSIVE, TYPE_SLASH}}));
            styles.put(10, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_CRUSH}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_CRUSH}}));
            styles.put(11, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_SLASH}, {STYLE_CONTROLLED, TYPE_SLASH}, {STYLE_DEFENSIVE, TYPE_SLASH}}));
            styles.put(12, instance.new Style(new int[][]{{}})); // flowers and rubber chicken etc
            styles.put(13, instance.new Style(new int[][]{{}})); // mud pie
            styles.put(14, instance.new Style(new int[][]{{STYLE_CONTROLLED, TYPE_STAB}, {STYLE_CONTROLLED, TYPE_SLASH}, {STYLE_CONTROLLED, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_STAB}}));
            styles.put(15, instance.new Style(new int[][]{{STYLE_CONTROLLED, TYPE_STAB}, {STYLE_AGGRESSIVE, TYPE_SLASH}, {STYLE_DEFENSIVE, TYPE_STAB}}));
            styles.put(16, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_RANGED}, {STYLE_RAPID, TYPE_RANGED}, {STYLE_LONG_RANGE, TYPE_RANGED}}));
            styles.put(17, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_RANGED}, {STYLE_RAPID, TYPE_RANGED}, {STYLE_LONG_RANGE, TYPE_RANGED}}));
            styles.put(18, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_RANGED}, {STYLE_RAPID, TYPE_RANGED}, {STYLE_LONG_RANGE, TYPE_RANGED}}));
            styles.put(19, instance.new Style(new int[][]{{}})); // chins
            styles.put(20, instance.new Style(new int[][]{{}})); // fixed device
            styles.put(21, instance.new Style(new int[][]{{}})); // salamanders
            styles.put(22, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_SLASH}, {STYLE_AGGRESSIVE, TYPE_STAB}, {STYLE_AGGRESSIVE, TYPE_CRUSH}, {STYLE_DEFENSIVE, TYPE_SLASH}}));
            styles.put(23, instance.new Style(new int[][]{{}})); // invandis flail
            styles.put(24, instance.new Style(new int[][]{{}})); // no existing item found
            styles.put(25, instance.new Style(new int[][]{{}})); // tridents
            styles.put(26, instance.new Style(new int[][]{{STYLE_ACCURATE, TYPE_STAB}, {STYLE_AGGRESSIVE, TYPE_SLASH}, {STYLE_DEFENSIVE, TYPE_CRUSH}})); // STAFF OF LIGHT
        }

    }

    public static class ArmourSet {
        int helmId;
        int chestId;
        int legsId;
        int weaponId;

        public ArmourSet(int helmId, int chestId, int legsId, int weaponId) {
            this.helmId = helmId;
            this.chestId = chestId;
            this.legsId = legsId;
            this.weaponId = weaponId;
        }
    }

    public static final ArmourSet DHAROKS = new ArmourSet(4716, 4720, 4722, 4718);
    public static final ArmourSet VERACS = new ArmourSet(4753, 4757, 4759, 4755);
    public static final ArmourSet GUTHANS = new ArmourSet(4724, 4728, 4730, 4726);
    public static final ArmourSet VOID_MELEE = new ArmourSet(0, 0, 0, 0);
    public static final ArmourSet VOID_RANGED = new ArmourSet(0, 0, 0, 0);
    public static final ArmourSet VOID_MAGIC = new ArmourSet(0, 0, 0, 0);

    public static boolean wearsArmourSet(Player player, ArmourSet set) {
        PossesedItem helm = player.getEquipment().get(Equipment.HELM_SLOT);
        PossesedItem chest = player.getEquipment().get(Equipment.CHEST_SLOT);
        PossesedItem legs = player.getEquipment().get(Equipment.BOTTOMS_SLOT);
        PossesedItem weapon = player.getEquipment().get(Equipment.WEAPON_SLOT);
        int helmId_ = -1;
        int chestId_ = -1;
        int legsId_ = -1;
        int weaponId_ = -1;
        if (helm != null)
            helmId_ = helm.getId();
        if (chest != null)
            chestId_ = chest.getId();
        if (legs != null)
            legsId_ = legs.getId();
        if (weapon != null)
            weaponId_ = weapon.getId();
        if (helmId_ == set.helmId
                && chestId_ == set.chestId
                && legsId_ == set.legsId
                && weaponId_ == set.weaponId)
            return true;
        return false;
    }
    
    public static boolean clippedProjectile(Entity entity, Entity victim) {
    	Tile start = entity.getCoverage().center();
    	Tile end = victim.getCoverage().center();
    	Tile currentTile = start;    	
    	NormalDirection globalDirection = null;
    	NormalDirection localDirection = null;
    	NormalDirection localDirectionInverse = null;
    	while (currentTile != end) {    		
    		globalDirection = Directions.directionFor(currentTile, end);
    		Tile nextTile = currentTile.translate(Directions.DIRECTION_DELTA_X[globalDirection.intValue()], Directions.DIRECTION_DELTA_Y[globalDirection.intValue()], 0);
    		localDirection = Directions.directionFor(currentTile, nextTile);	
    		localDirectionInverse = Directions.directionFor(nextTile, currentTile);
    		GameObject currentObject = Region.getWallObject(currentTile);
    		GameObject nextObject = Region.getWallObject(nextTile);
    		if (currentObject != null) {
    			if (nextObject != null) {
					if (!currentTile.canMove(localDirection, 1, false) || !nextTile.canMove(localDirectionInverse, 1, false))
						break;
    			} else {
    				if (!currentTile.canMove(localDirection, 1, false) || !nextTile.canMove(localDirectionInverse, 1, false)) 
	    				break;
    			}
    		} else if (nextObject != null) {
    			if (!currentTile.canMove(localDirection, 1, false) || !nextTile.canMove(localDirectionInverse, 1, false))
					break;
    		}
    		if (currentTile.canMove(localDirection, 1, false) && currentTile.canMove(localDirectionInverse, 1, false)) {
    			currentTile = nextTile;
    			continue;
    		} else {
    			boolean solid = (Region.getAbsoluteClipping(nextTile.getX(), nextTile.getY(), nextTile.getZ()) & 0x20000) != 0;
    			boolean solid2 = (Region.getAbsoluteClipping(currentTile.getX(), currentTile.getY(), currentTile.getZ()) & 0x20000) != 0;
    			if (!solid && !solid2) {
    				currentTile = nextTile;
    				continue;
    			} else 
    				break;
    		}
    	}  	
    	return currentTile == end;
    }

}
