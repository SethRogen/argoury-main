/**
 *
 */
package com.runescape.adapter.protocol.cache.format;

import com.runescape.Static;
import com.runescape.logic.object.ObjectDefinition;
import com.runescape.utility.Streams;

import java.nio.ByteBuffer;

/**
 * @author Lazaro
 */
public class ObjectDefinitionAdapter {
	
	public static ObjectDefinition forId(int id) {
		ObjectDefinition def = new ObjectDefinition();
		def.id = id;

		byte[] data = Static.rs2Cache.getIndex(16).getArchivedFile(id >>> 8, id & 0xff);
		if (data != null) {
			load(def, ByteBuffer.wrap(data));
		}
		if (def.clippingFlag && def.name != null && def.name.contains("booth")) {
			def.clippingFlag = false;
		}
		if (def.clippingFlag) {
			def.actionCount = 0;
			def.walkable = false;
		}
		return def;
	}


	private static void load(ObjectDefinition def, ByteBuffer buffer) {
		int opcode;
		while ((opcode = (buffer.get() & 0xff)) != 0) {
			parseOpcode(def, opcode, buffer);
		}
	}

	private static void parseOpcode(ObjectDefinition def, int opcode, ByteBuffer buffer) {
		if (opcode != 1 && opcode != 5) {
			if ((opcode ^ 0xffffffff) != -3) {
				if ((opcode ^ 0xffffffff) != -15) {
					if (opcode == 15)
						def.sizeY = buffer.get() & 0xff;
					else if ((opcode ^ 0xffffffff) == -18) {
						def.actionCount = 0;
						def.walkable = false;
					} else if (opcode != 18) {
						if ((opcode ^ 0xffffffff) == -20)
							buffer.get();
						else if ((opcode ^ 0xffffffff) == -25) {
							buffer.getShort();
						} else if ((opcode ^ 0xffffffff) == -28)
							def.actionCount = 1;
						else if ((opcode ^ 0xffffffff) != -29) {
							if (opcode != 29) {
								if ((opcode ^ 0xffffffff) == -40)
									buffer.get();
								else if (opcode < 30 || (opcode ^ 0xffffffff) <= -36) {
									if ((opcode ^ 0xffffffff) != -41) {
										if (opcode == 41) {
											int i_49_ = buffer.get() & 0xff;
											for (int i_50_ = 0; i_50_ < i_49_; i_50_++) {
												buffer.getShort();
												buffer.getShort();
											}
										} else if (opcode != 42) {
											if ((opcode ^ 0xffffffff) != -65) {
												if (opcode != 65) {
													if ((opcode ^ 0xffffffff) != -67) {
														if (opcode != 67) {
															if ((opcode ^ 0xffffffff) != -70) {
																if (opcode == 70)
																	buffer.getShort();
																else if ((opcode ^ 0xffffffff) == -72)
																	buffer.getShort();
																else if (opcode != 72) {
																	if ((opcode ^ 0xffffffff) != -74) {
																		if (opcode == 74)
																			def.clippingFlag = true;
																		else if (opcode == 75)
																			buffer.get();
																		else if ((opcode ^ 0xffffffff) == -78 || (opcode ^ 0xffffffff) == -93) {
																			buffer.getShort();
																			buffer.getShort();


																			if ((opcode ^ 0xffffffff) == -93) {
																				buffer.getShort();

																			}
																			int i_52_ = buffer.get() & 0xff;
																			for (int i_53_ = 0; i_52_ >= i_53_; i_53_++) {
																				buffer.getShort();
																			}
																		} else if ((opcode ^ 0xffffffff) != -79) {
																			if ((opcode ^ 0xffffffff) == -80) {
																				buffer.getShort();
																				buffer.getShort();
																				buffer.get();
																				int i_54_ = buffer.get() & 0xff;
																				for (int i_55_ = 0; i_54_ > i_55_; i_55_++)
																					buffer.getShort();
																			} else if ((opcode ^ 0xffffffff) == -82) {
																				buffer.get();
																			} else if ((opcode ^ 0xffffffff) != -83) {
																				if (opcode == 93) {
																					buffer.getShort();
																				} else if ((opcode ^ 0xffffffff) != -96) {
																					if ((opcode ^ 0xffffffff) != -98) {
																						if (opcode != 98) {
																							if ((opcode ^ 0xffffffff) == -100) {
																								buffer.get();
																								buffer.getShort();
																							} else if (opcode == 100) {
																								buffer.get();
																								buffer.getShort();
																							} else if (opcode == 101)
																								buffer.get();
																							else if ((opcode ^ 0xffffffff) == -103)
																								buffer.getShort();
																							else if ((opcode ^ 0xffffffff) == -105)
																								buffer.get();
																							else if ((opcode ^ 0xffffffff) == -107) {
																								int i_56_ = buffer.get() & 0xff;
																								for (int i_57_ = 0; (i_56_ ^ 0xffffffff) < (i_57_ ^ 0xffffffff); i_57_++) {
																									buffer.getShort();
																									buffer.get();
																								}
																							} else if (opcode == 107)
																								def.miniMapSpriteId = buffer.getShort();
																							else if ((opcode ^ 0xffffffff) <= -151 && (opcode ^ 0xffffffff) > -156) {
																								def.actions[opcode - 150] = Streams.readString(buffer);
																							} else if ((opcode ^ 0xffffffff) != -161) {
																								if (opcode != 162) {
																									if (opcode != 163) {
																										if (opcode == 164)
																											buffer.getShort();
																										else if (opcode == 165)
																											buffer.getShort();
																										else if ((opcode ^ 0xffffffff) == -167)
																											buffer.getShort();
																										else if ((opcode ^ 0xffffffff) != -168) {
																											if ((opcode ^ 0xffffffff) != -170) {
																												if (opcode == 170)
																													Streams.readSmart(buffer);
																												else if ((opcode ^ 0xffffffff) != -172) {
																													if (opcode == 173) {
																														buffer.getShort();
																														buffer.getShort();
																													} else if ((opcode ^ 0xffffffff) != -178) {
																														if (opcode == 178)
																															buffer.get();
																														else if ((opcode ^ 0xffffffff) == -250) {
																															int i_61_ = (buffer.get() & 0xff);
																															for (int i_63_ = 0; (i_61_ ^ 0xffffffff) < (i_63_ ^ 0xffffffff); i_63_++) {
																																boolean bool = (buffer.get() & 0xff) == 1;
																																@SuppressWarnings("unused") int i_64_ = (buffer.get() & 0xff) << 24 | (buffer.get() & 0xff) << 16 | (buffer.get() & 0xff);
																																if (!bool)
																																	buffer.getInt();
																																else
																																	Streams.readString(buffer);
																															}
																														}
																													}
																												} else
																													Streams.readSmart(buffer);
																											}
																										} else
																											buffer.getShort();
																									} else {
																										buffer.get();
																										buffer.get();
																										buffer.get();
																										buffer.get();
																									}
																								} else {
																									buffer.getInt();
																								}
																							} else {
																								int i_64_ = buffer.get() & 0xff;
																								for (int i_65_ = 0; (i_64_ ^ 0xffffffff) < (i_65_ ^ 0xffffffff); i_65_++)
																									buffer.getShort();
																							}
																						}
																					}
																				} else {
																					buffer.getShort();
																				}
																			}
																		} else {
																			buffer.getShort();
																			buffer.get();
																		}
																	}
																} else
																	buffer.getShort();
															} else
																def.walkToData = buffer.get() & 0xff;
														} else
															buffer.getShort();
													} else
														buffer.getShort();
												} else
													buffer.getShort();
											} else {
												//def.clippingFlag2 = false;
											}
										} else {
											int i_66_ = buffer.get() & 0xff;
											for (int i_67_ = 0; ((i_67_ ^ 0xffffffff) > (i_66_ ^ 0xffffffff)); i_67_++)
												buffer.get();
										}
									} else {
										int i_68_ = buffer.get();
										for (int i_69_ = 0; i_69_ < i_68_; i_69_++) {
											buffer.getShort();
											buffer.getShort();
										}
									}
								} else
									def.actions[-30 + opcode] = Streams.readString(buffer);
							} else
								buffer.get();
						} else
							buffer.get();
					} else
						def.walkable = false;
				} else
					def.sizeX = buffer.get() & 0xff;
			} else
				def.name = Streams.readString(buffer);
		} else {
			boolean aBoolean1492 = false;
			if (opcode == 5 && aBoolean1492)
				readSkip(buffer);
			int i_71_ = (buffer.get() & 0xff);
			for (int i_72_ = 0; i_71_ > i_72_; i_72_++) {
				buffer.get();
				int i_73_ = (buffer.get() & 0xff);
				for (int i_74_ = 0; (i_74_ ^ 0xffffffff) > (i_73_ ^ 0xffffffff); i_74_++)
					buffer.getShort();
			}
			if (opcode == 5 && !aBoolean1492)
				readSkip(buffer);
		}
	}

	private static void readSkip(ByteBuffer buffer) {
		int length = buffer.get() & 0xff;
		for (int index = 0; index < length; index++) {
			buffer.position(buffer.position() + 1);
			buffer.position(buffer.position() + ((buffer.get() & 0xff) * 2));
		}
	}

}
