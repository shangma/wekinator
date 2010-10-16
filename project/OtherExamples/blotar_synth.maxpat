{
	"patcher" : 	{
		"fileversion" : 1,
		"rect" : [ 31.0, 44.0, 977.0, 659.0 ],
		"bglocked" : 0,
		"defrect" : [ 31.0, 44.0, 977.0, 659.0 ],
		"openrect" : [ 31.0, 44.0, 977.0, 659.0 ],
		"openinpresentation" : 0,
		"default_fontsize" : 12.0,
		"default_fontface" : 0,
		"default_fontname" : "Arial",
		"gridonopen" : 0,
		"gridsize" : [ 15.0, 15.0 ],
		"gridsnaponopen" : 0,
		"toolbarvisible" : 1,
		"boxanimatetime" : 200,
		"imprint" : 0,
		"enablehscroll" : 1,
		"enablevscroll" : 1,
		"boxes" : [ 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "Don't change port -- keep at 6448. Send to localhost if Wekinator is on same machine, otherwise use IP address of Wekinator's machine.",
					"linecount" : 2,
					"presentation_linecount" : 7,
					"presentation_rect" : [ 57.0, 427.0, 101.0, 79.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 541.0, 506.0, 357.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-67",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "This part receives new parameter settings from Weinator. Don't change port or OSC message name!",
					"linecount" : 2,
					"presentation_linecount" : 5,
					"presentation_rect" : [ 57.0, 427.0, 101.0, 58.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 375.0, 35.0, 244.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-66",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "This part sends current param values to the Wekinator every 100ms, set by metro",
					"presentation_linecount" : 4,
					"presentation_rect" : [ 42.0, 412.0, 99.0, 48.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 347.0, 337.0, 357.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-65",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "BLOTAR PARAMETERS:",
					"presentation_rect" : [ 32.0, 198.0, 138.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 324.0, 181.0, 138.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-64",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "volume",
					"presentation_rect" : [ 40.0, 76.0, 73.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 233.0, 308.0, 73.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-63",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "toggle",
					"presentation_rect" : [ 257.0, 488.0, 20.0, 20.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 326.0, 437.0, 20.0, 20.0 ],
					"presentation" : 1,
					"id" : "obj-59",
					"numoutlets" : 1,
					"outlettype" : [ "int" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "metro 100",
					"numinlets" : 2,
					"patching_rect" : [ 325.0, 474.0, 65.0, 20.0 ],
					"id" : "obj-62",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "bang" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "standalone",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 613.0, 400.0, 69.0, 20.0 ],
					"id" : "obj-1",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 12.0,
					"saved_object_attributes" : 					{
						"usesearchpath" : 1,
						"noloadbangdefeating" : 0,
						"searchformissingfiles" : 1,
						"statusvisible" : 1,
						"overdrive" : 0,
						"cantclosetoplevelpatchers" : 1,
						"preffilename" : "Max 5 Preferences",
						"allwindowsactive" : 0,
						"midisupport" : 1,
						"audiosupport" : 1
					}

				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "$1 50",
					"numinlets" : 2,
					"patching_rect" : [ 490.0, 158.0, 41.0, 18.0 ],
					"id" : "obj-61",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "$1 50",
					"numinlets" : 2,
					"hidden" : 1,
					"patching_rect" : [ 370.0, 162.0, 41.0, 18.0 ],
					"id" : "obj-60",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "line 1. 80",
					"numinlets" : 3,
					"hidden" : 1,
					"patching_rect" : [ 474.0, 187.0, 59.0, 20.0 ],
					"id" : "obj-56",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"outlettype" : [ "", "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "line 1. 80",
					"numinlets" : 3,
					"hidden" : 1,
					"patching_rect" : [ 335.0, 186.0, 59.0, 20.0 ],
					"id" : "obj-55",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"outlettype" : [ "", "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "preset",
					"presentation_rect" : [ 229.0, 118.0, 46.0, 39.0 ],
					"bubblesize" : 8,
					"numinlets" : 1,
					"patching_rect" : [ 34.0, 381.0, 46.0, 39.0 ],
					"margin" : 4,
					"presentation" : 1,
					"id" : "obj-52",
					"numoutlets" : 4,
					"spacing" : 2,
					"outlettype" : [ "preset", "int", "preset", "int" ],
					"preset_data" : [ 						{
							"number" : 1,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 62, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 0.06, 5, "obj-30", "flonum", "float", 293.664764, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 917.702393, 5, "obj-19", "flonum", "float", 1.059998, 5, "obj-18", "flonum", "float", 0.31, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 0.0, 5, "obj-15", "flonum", "float", 0.17, 5, "obj-14", "flonum", "float", 0.49, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 2,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 65, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 349.228241, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 891.0, 5, "obj-19", "flonum", "float", 0.829999, 5, "obj-18", "flonum", "float", 0.55, 5, "obj-17", "flonum", "float", 0.22, 5, "obj-16", "flonum", "float", 2.0, 5, "obj-15", "flonum", "float", 0.95, 5, "obj-14", "flonum", "float", 0.63, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 3,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 72, 5, "obj-32", "flonum", "float", 8.0, 5, "obj-31", "flonum", "float", 0.1, 5, "obj-30", "flonum", "float", 300.0, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 1000.0, 5, "obj-19", "flonum", "float", 0.82, 5, "obj-18", "flonum", "float", 1.63, 5, "obj-17", "flonum", "float", 0.64, 5, "obj-16", "flonum", "float", 3.0, 5, "obj-15", "flonum", "float", 0.99, 5, "obj-14", "flonum", "float", 0.06, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 4,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 43, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 300.0, 5, "obj-28", "number", "int", 11, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 900.0, 5, "obj-19", "flonum", "float", 0.0, 5, "obj-18", "flonum", "float", 0.37, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 5.0, 5, "obj-15", "flonum", "float", 3.409999, 5, "obj-14", "flonum", "float", 0.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 5,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 84, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 1046.502319, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 3270.319824, 5, "obj-19", "flonum", "float", 0.0, 5, "obj-18", "flonum", "float", 1.63, 5, "obj-17", "flonum", "float", 0.64, 5, "obj-16", "flonum", "float", 3.0, 5, "obj-15", "flonum", "float", 2.0, 5, "obj-14", "flonum", "float", 0.0, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 0.0 ]
						}
, 						{
							"number" : 6,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 43, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 47.0, 5, "obj-30", "flonum", "float", 97.998856, 5, "obj-28", "number", "int", 6, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 42.0, 5, "obj-19", "flonum", "float", 1.53, 5, "obj-18", "flonum", "float", 0.059999, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 78.0, 5, "obj-15", "flonum", "float", 3.309998, 5, "obj-14", "flonum", "float", 109.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 0.02 ]
						}
, 						{
							"number" : 7,
							"data" : [ 5, "obj-33", "kslider", "int", 55, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 0.06, 5, "obj-30", "flonum", "float", 195.997711, 5, "obj-28", "number", "int", 6, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 42.0, 5, "obj-19", "flonum", "float", 1.059998, 5, "obj-18", "flonum", "float", 0.31, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 9.0, 5, "obj-15", "flonum", "float", 3.309998, 5, "obj-14", "flonum", "float", 109.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 0.02 ]
						}
, 						{
							"number" : 8,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 43, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 47.0, 5, "obj-30", "flonum", "float", 250.0, 5, "obj-28", "number", "int", 6, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 800.0, 5, "obj-19", "flonum", "float", 1.53, 5, "obj-18", "flonum", "float", 0.059999, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 78.0, 5, "obj-15", "flonum", "float", 3.309998, 5, "obj-14", "flonum", "float", 109.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 0.02 ]
						}
, 						{
							"number" : 9,
							"data" : [ 5, "obj-33", "kslider", "int", 72, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 523.25116, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 1635.159912, 5, "obj-19", "flonum", "float", 0.829999, 5, "obj-18", "flonum", "float", 0.55, 5, "obj-17", "flonum", "float", 0.22, 5, "obj-16", "flonum", "float", 2.0, 5, "obj-15", "flonum", "float", 0.95, 5, "obj-14", "flonum", "float", 0.63, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 10,
							"data" : [ 5, "obj-33", "kslider", "int", 84, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 1046.502319, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 3270.319824, 5, "obj-19", "flonum", "float", 0.0, 5, "obj-18", "flonum", "float", 1.63, 5, "obj-17", "flonum", "float", 0.64, 5, "obj-16", "flonum", "float", 3.0, 5, "obj-15", "flonum", "float", 2.0, 5, "obj-14", "flonum", "float", 0.0, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 0.0 ]
						}
, 						{
							"number" : 11,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 62, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 0.06, 5, "obj-30", "flonum", "float", 293.664764, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 917.702393, 5, "obj-19", "flonum", "float", 0.0, 5, "obj-18", "flonum", "float", 0.31, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 0.0, 5, "obj-15", "flonum", "float", 0.17, 5, "obj-14", "flonum", "float", 0.49, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 12,
							"data" : [ 6, "obj-40", "gain~", "list", 70, 10.0, 5, "obj-33", "kslider", "int", 47, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 0.06, 5, "obj-30", "flonum", "float", 123.470825, 5, "obj-28", "number", "int", 6, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 42.0, 5, "obj-19", "flonum", "float", 1.059998, 5, "obj-18", "flonum", "float", 0.31, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 9.0, 5, "obj-15", "flonum", "float", 3.309998, 5, "obj-14", "flonum", "float", 109.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", -1.0 ]
						}
 ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "button",
					"numinlets" : 1,
					"patching_rect" : [ 369.0, 358.0, 20.0, 20.0 ],
					"id" : "obj-7",
					"numoutlets" : 1,
					"outlettype" : [ "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "t b s",
					"numinlets" : 1,
					"patching_rect" : [ 394.0, 423.0, 33.0, 20.0 ],
					"id" : "obj-82",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"outlettype" : [ "bang", "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "OpenSoundControl 2000",
					"numinlets" : 1,
					"patching_rect" : [ 394.0, 468.0, 143.0, 20.0 ],
					"id" : "obj-77",
					"fontname" : "Arial",
					"numoutlets" : 3,
					"outlettype" : [ "", "", "OSCTimeTag" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "prepend /realValue",
					"numinlets" : 1,
					"patching_rect" : [ 394.0, 391.0, 112.0, 20.0 ],
					"id" : "obj-74",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "pack 1. 1. 1. 1. 1. 1. 1. 1. 1.",
					"numinlets" : 9,
					"patching_rect" : [ 394.0, 359.0, 156.0, 20.0 ],
					"id" : "obj-58",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "udpsend localhost 6448",
					"numinlets" : 1,
					"patching_rect" : [ 394.0, 513.0, 137.0, 20.0 ],
					"id" : "obj-53",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "unpack 1. 1. 1. 1. 1. 1. 1. 1. 1.",
					"numinlets" : 1,
					"patching_rect" : [ 382.0, 131.0, 169.0, 20.0 ],
					"id" : "obj-57",
					"fontname" : "Arial",
					"numoutlets" : 9,
					"outlettype" : [ "float", "float", "float", "float", "float", "float", "float", "float", "float" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "route /OSCSynth/params /OSCSynth/startSendingParams",
					"numinlets" : 1,
					"patching_rect" : [ 382.0, 99.0, 243.0, 17.0 ],
					"id" : "obj-54",
					"fontname" : "Arial",
					"numoutlets" : 3,
					"outlettype" : [ "", "", "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "udpreceive 12000",
					"numinlets" : 1,
					"patching_rect" : [ 382.0, 69.0, 106.0, 20.0 ],
					"id" : "obj-51",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 12.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "1",
					"numinlets" : 2,
					"patching_rect" : [ 325.0, 416.0, 16.0, 15.0 ],
					"id" : "obj-2",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "loadbang",
					"numinlets" : 1,
					"patching_rect" : [ 317.0, 389.0, 53.0, 17.0 ],
					"id" : "obj-3",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "bang" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "blotar~",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 660.0, 30.0, 76.0, 23.0 ],
					"id" : "obj-4",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 14.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "flute-guitar hybrid model.",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 660.0, 51.0, 125.0, 17.0 ],
					"id" : "obj-5",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "• blotar~ is an example of what you can do with the synthesis toolkit. it's parameters morph between an electric guitar model and a flute.",
					"linecount" : 5,
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 660.0, 78.0, 146.0, 58.0 ],
					"id" : "obj-6",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"maximum" : 1.0,
					"presentation_rect" : [ 133.0, 419.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 636.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-8",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"minimum" : -1.0,
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "clear",
					"numinlets" : 2,
					"hidden" : 1,
					"patching_rect" : [ 42.0, 167.0, 31.0, 15.0 ],
					"id" : "obj-9",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "toggle",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 234.0, 210.0, 15.0, 15.0 ],
					"id" : "obj-10",
					"numoutlets" : 1,
					"outlettype" : [ "int" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "gate",
					"numinlets" : 2,
					"hidden" : 1,
					"patching_rect" : [ 250.0, 171.0, 27.0, 17.0 ],
					"id" : "obj-11",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "preset",
					"presentation_rect" : [ 178.0, 118.0, 46.0, 39.0 ],
					"bubblesize" : 8,
					"numinlets" : 1,
					"patching_rect" : [ 36.0, 319.0, 46.0, 39.0 ],
					"margin" : 4,
					"presentation" : 1,
					"id" : "obj-12",
					"numoutlets" : 4,
					"spacing" : 2,
					"outlettype" : [ "preset", "int", "preset", "int" ],
					"preset_data" : [ 						{
							"number" : 1,
							"data" : [ 5, "obj-33", "kslider", "int", 62, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 0.06, 5, "obj-30", "flonum", "float", 293.664764, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 917.702393, 5, "obj-19", "flonum", "float", 1.059998, 5, "obj-18", "flonum", "float", 0.31, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 0.0, 5, "obj-15", "flonum", "float", 0.17, 5, "obj-14", "flonum", "float", 0.49, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 2,
							"data" : [ 5, "obj-33", "kslider", "int", 72, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 523.25116, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 232.0, 5, "obj-19", "flonum", "float", 0.0, 5, "obj-18", "flonum", "float", 1.63, 5, "obj-17", "flonum", "float", 0.64, 5, "obj-16", "flonum", "float", 3.0, 5, "obj-15", "flonum", "float", 0.99, 5, "obj-14", "flonum", "float", 0.06, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 3,
							"data" : [ 5, "obj-33", "kslider", "int", 65, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 349.228241, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 132.0, 5, "obj-19", "flonum", "float", 0.829999, 5, "obj-18", "flonum", "float", 0.55, 5, "obj-17", "flonum", "float", 0.22, 5, "obj-16", "flonum", "float", 2.0, 5, "obj-15", "flonum", "float", 0.95, 5, "obj-14", "flonum", "float", 0.63, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 4,
							"data" : [ 5, "obj-33", "kslider", "int", 72, 5, "obj-32", "flonum", "float", 8.0, 5, "obj-31", "flonum", "float", 0.1, 5, "obj-30", "flonum", "float", 523.25116, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 1635.159912, 5, "obj-19", "flonum", "float", 0.82, 5, "obj-18", "flonum", "float", 1.63, 5, "obj-17", "flonum", "float", 0.64, 5, "obj-16", "flonum", "float", 3.0, 5, "obj-15", "flonum", "float", 0.99, 5, "obj-14", "flonum", "float", 0.06, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 5,
							"data" : [ 5, "obj-33", "kslider", "int", 43, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 97.998856, 5, "obj-28", "number", "int", 11, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 306.246429, 5, "obj-19", "flonum", "float", 0.0, 5, "obj-18", "flonum", "float", 0.37, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 5.0, 5, "obj-15", "flonum", "float", 3.409999, 5, "obj-14", "flonum", "float", 0.0, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 6,
							"data" : [ 5, "obj-33", "kslider", "int", 47, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 0.06, 5, "obj-30", "flonum", "float", 123.470825, 5, "obj-28", "number", "int", 6, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 42.0, 5, "obj-19", "flonum", "float", 1.059998, 5, "obj-18", "flonum", "float", 0.31, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 9.0, 5, "obj-15", "flonum", "float", 3.309998, 5, "obj-14", "flonum", "float", 109.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", -1.0 ]
						}
, 						{
							"number" : 7,
							"data" : [ 5, "obj-33", "kslider", "int", 55, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 0.06, 5, "obj-30", "flonum", "float", 195.997711, 5, "obj-28", "number", "int", 6, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 42.0, 5, "obj-19", "flonum", "float", 1.059998, 5, "obj-18", "flonum", "float", 0.31, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 9.0, 5, "obj-15", "flonum", "float", 3.309998, 5, "obj-14", "flonum", "float", 109.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 0.02 ]
						}
, 						{
							"number" : 8,
							"data" : [ 5, "obj-33", "kslider", "int", 43, 5, "obj-32", "flonum", "float", 5.0, 5, "obj-31", "flonum", "float", 47.0, 5, "obj-30", "flonum", "float", 97.998856, 5, "obj-28", "number", "int", 6, 5, "obj-21", "flonum", "float", 0.130001, 5, "obj-20", "flonum", "float", 42.0, 5, "obj-19", "flonum", "float", 1.53, 5, "obj-18", "flonum", "float", 0.059999, 5, "obj-17", "flonum", "float", 0.78, 5, "obj-16", "flonum", "float", 78.0, 5, "obj-15", "flonum", "float", 3.309998, 5, "obj-14", "flonum", "float", 109.0, 5, "obj-10", "toggle", "int", 0, 5, "obj-8", "flonum", "float", 0.02 ]
						}
, 						{
							"number" : 9,
							"data" : [ 5, "obj-33", "kslider", "int", 72, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 523.25116, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 1635.159912, 5, "obj-19", "flonum", "float", 0.829999, 5, "obj-18", "flonum", "float", 0.55, 5, "obj-17", "flonum", "float", 0.22, 5, "obj-16", "flonum", "float", 2.0, 5, "obj-15", "flonum", "float", 0.95, 5, "obj-14", "flonum", "float", 0.63, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 1.0 ]
						}
, 						{
							"number" : 10,
							"data" : [ 5, "obj-33", "kslider", "int", 84, 5, "obj-32", "flonum", "float", 0.0, 5, "obj-31", "flonum", "float", 0.0, 5, "obj-30", "flonum", "float", 1046.502319, 5, "obj-28", "number", "int", 0, 5, "obj-21", "flonum", "float", 0.0, 5, "obj-20", "flonum", "float", 3270.319824, 5, "obj-19", "flonum", "float", 0.0, 5, "obj-18", "flonum", "float", 1.63, 5, "obj-17", "flonum", "float", 0.64, 5, "obj-16", "flonum", "float", 3.0, 5, "obj-15", "flonum", "float", 2.0, 5, "obj-14", "flonum", "float", 0.0, 5, "obj-10", "toggle", "int", 1, 5, "obj-8", "flonum", "float", 0.0 ]
						}
 ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "mtof",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 53.0, 139.0, 29.0, 17.0 ],
					"id" : "obj-13",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 85.0, 369.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 526.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-14",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 140.0, 369.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 581.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-15",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 31.0, 256.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 31.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-16",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 86.0, 256.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 86.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-17",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 141.0, 256.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 141.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-18",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 196.0, 256.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 196.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-19",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 31.0, 310.0, 45.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 251.0, 256.0, 45.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-20",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 86.0, 310.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 306.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-21",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "Bore/distortion gain",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"presentation_rect" : [ 145.0, 340.0, 72.0, 27.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 586.0, 227.0, 72.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-22",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "jet/feedback coeff",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"presentation_rect" : [ 81.0, 341.0, 60.0, 27.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 522.0, 228.0, 60.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-23",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "Flute/string freq",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"presentation_rect" : [ 26.0, 341.0, 56.0, 27.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 467.0, 228.0, 56.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-24",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "vib amp",
					"presentation_rect" : [ 190.0, 294.0, 47.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 410.0, 240.0, 47.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-25",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "vib freq",
					"presentation_rect" : [ 135.0, 293.0, 47.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 355.0, 239.0, 47.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-26",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "prepend mic",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 41.0, 205.0, 62.0, 17.0 ],
					"id" : "obj-27",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "number",
					"maximum" : 11,
					"triscale" : 0.9,
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 41.0, 187.0, 35.0, 17.0 ],
					"id" : "obj-28",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "int", "bang" ],
					"minimum" : 0,
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "button",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 31.0, 141.0, 15.0, 15.0 ],
					"id" : "obj-29",
					"numoutlets" : 1,
					"outlettype" : [ "bang" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 30.0, 369.0, 47.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 471.0, 256.0, 47.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-30",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 196.0, 310.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 416.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-31",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "flonum",
					"presentation_rect" : [ 141.0, 310.0, 35.0, 17.0 ],
					"triscale" : 0.9,
					"numinlets" : 1,
					"patching_rect" : [ 361.0, 256.0, 35.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-32",
					"fontname" : "Arial",
					"numoutlets" : 2,
					"htextcolor" : [ 0.870588, 0.870588, 0.870588, 1.0 ],
					"outlettype" : [ "float", "bang" ],
					"bgcolor" : [ 0.866667, 0.866667, 0.866667, 1.0 ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "kslider",
					"presentation_rect" : [ 24.0, 477.0, 294.0, 34.0 ],
					"range" : 72,
					"numinlets" : 2,
					"patching_rect" : [ 31.0, 98.0, 294.0, 34.0 ],
					"presentation" : 1,
					"id" : "obj-33",
					"hkeycolor" : [ 0.501961, 0.501961, 0.501961, 1.0 ],
					"numoutlets" : 2,
					"outlettype" : [ "int", "int" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "stop",
					"presentation_rect" : [ 136.0, 46.0, 27.0, 15.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 239.0, 432.0, 27.0, 15.0 ],
					"presentation" : 1,
					"id" : "obj-34",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "noise gain",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"presentation_rect" : [ 88.0, 281.0, 47.0, 27.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 308.0, 227.0, 47.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-35",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "Jet/feedback freq",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"presentation_rect" : [ 24.0, 281.0, 62.0, 27.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 244.0, 227.0, 62.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-36",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "breath pressure",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"presentation_rect" : [ 191.0, 227.0, 47.0, 27.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 191.0, 227.0, 47.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-37",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "body size",
					"presentation_rect" : [ 132.0, 238.0, 49.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 132.0, 238.0, 49.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-38",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "pluck position",
					"linecount" : 2,
					"presentation_linecount" : 2,
					"presentation_rect" : [ 79.0, 226.0, 49.0, 27.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 79.0, 226.0, 49.0, 27.0 ],
					"presentation" : 1,
					"id" : "obj-39",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "gain~",
					"presentation_rect" : [ 35.0, 96.0, 77.0, 90.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 156.0, 307.0, 77.0, 90.0 ],
					"presentation" : 1,
					"id" : "obj-40",
					"numoutlets" : 2,
					"orientation" : 2,
					"outlettype" : [ "signal", "int" ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "message",
					"text" : "startwindow",
					"presentation_rect" : [ 37.0, 46.0, 65.0, 15.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 140.0, 432.0, 65.0, 15.0 ],
					"presentation" : 1,
					"id" : "obj-41",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "dac~",
					"presentation_rect" : [ 106.0, 46.0, 29.0, 17.0 ],
					"numinlets" : 2,
					"patching_rect" : [ 209.0, 432.0, 29.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-42",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "meter~",
					"presentation_rect" : [ 16.0, 94.0, 17.0, 91.0 ],
					"coldcolor" : [ 0.0, 0.658824, 0.0, 1.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 139.0, 307.0, 17.0, 91.0 ],
					"presentation" : 1,
					"tepidcolor" : [ 0.6, 0.729412, 0.0, 1.0 ],
					"id" : "obj-43",
					"numoutlets" : 1,
					"interval" : 100,
					"warmcolor" : [ 0.85098, 0.85098, 0.0, 1.0 ],
					"outlettype" : [ "float" ],
					"bgcolor" : [ 0.407843, 0.407843, 0.407843, 1.0 ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "blotar~",
					"numinlets" : 12,
					"patching_rect" : [ 31.0, 283.0, 618.0, 17.0 ],
					"id" : "obj-44",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "signal" ],
					"fontsize" : 9.0,
					"color" : [ 0.8, 0.611765, 0.380392, 1.0 ]
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "pluck amp",
					"presentation_rect" : [ 23.0, 228.0, 52.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 23.0, 228.0, 52.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-45",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "link with bore freq",
					"numinlets" : 1,
					"hidden" : 1,
					"patching_rect" : [ 253.0, 212.0, 100.0, 17.0 ],
					"id" : "obj-46",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "newobj",
					"text" : "/ 0.32",
					"numinlets" : 2,
					"hidden" : 1,
					"patching_rect" : [ 250.0, 193.0, 38.0, 17.0 ],
					"id" : "obj-47",
					"fontname" : "Arial",
					"numoutlets" : 1,
					"outlettype" : [ "float" ],
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "friggin' filter ratio (interpolates between the one-pole filter of the flute and the lowpass fliter of the plucked string. nutty.",
					"linecount" : 6,
					"presentation_linecount" : 6,
					"presentation_rect" : [ 27.0, 397.0, 99.0, 69.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 671.0, 222.0, 99.0, 69.0 ],
					"presentation" : 1,
					"id" : "obj-48",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "• start audio.",
					"presentation_rect" : [ 40.0, 26.0, 73.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 143.0, 412.0, 73.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-49",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
, 			{
				"box" : 				{
					"maxclass" : "comment",
					"text" : "• try these presets.",
					"presentation_rect" : [ 181.0, 93.0, 100.0, 17.0 ],
					"numinlets" : 1,
					"patching_rect" : [ 36.0, 362.0, 100.0, 17.0 ],
					"presentation" : 1,
					"id" : "obj-50",
					"fontname" : "Arial",
					"numoutlets" : 0,
					"fontsize" : 9.0
				}

			}
 ],
		"lines" : [ 			{
				"patchline" : 				{
					"source" : [ "obj-61", 0 ],
					"destination" : [ "obj-56", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 5 ],
					"destination" : [ "obj-61", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-60", 0 ],
					"destination" : [ "obj-55", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 1 ],
					"destination" : [ "obj-60", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-55", 0 ],
					"destination" : [ "obj-20", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-56", 0 ],
					"destination" : [ "obj-30", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-7", 0 ],
					"destination" : [ "obj-58", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-33", 0 ],
					"destination" : [ "obj-29", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-29", 0 ],
					"destination" : [ "obj-44", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-27", 0 ],
					"destination" : [ "obj-44", 0 ],
					"hidden" : 1,
					"midpoints" : [ 50.5, 277.0, 40.5, 277.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-16", 0 ],
					"destination" : [ "obj-44", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-9", 0 ],
					"destination" : [ "obj-44", 0 ],
					"hidden" : 1,
					"midpoints" : [ 51.5, 277.0, 40.5, 277.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-2", 0 ],
					"destination" : [ "obj-12", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-28", 0 ],
					"destination" : [ "obj-27", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-33", 0 ],
					"destination" : [ "obj-13", 0 ],
					"hidden" : 1,
					"midpoints" : [ 40.5, 136.0, 62.5, 136.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-17", 0 ],
					"destination" : [ "obj-44", 1 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-40", 0 ],
					"destination" : [ "obj-43", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-18", 0 ],
					"destination" : [ "obj-44", 2 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-12", 2 ],
					"destination" : [ "obj-40", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-44", 0 ],
					"destination" : [ "obj-40", 0 ],
					"hidden" : 1,
					"midpoints" : [ 40.5, 303.0, 165.5, 303.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-19", 0 ],
					"destination" : [ "obj-44", 3 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-40", 0 ],
					"destination" : [ "obj-42", 0 ],
					"hidden" : 1,
					"midpoints" : [ 165.5, 421.0, 218.5, 421.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-41", 0 ],
					"destination" : [ "obj-42", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-34", 0 ],
					"destination" : [ "obj-42", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-40", 0 ],
					"destination" : [ "obj-42", 1 ],
					"hidden" : 1,
					"midpoints" : [ 165.5, 421.0, 228.5, 421.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-10", 0 ],
					"destination" : [ "obj-11", 0 ],
					"hidden" : 1,
					"midpoints" : [ 243.0, 167.0, 259.5, 167.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-11", 0 ],
					"destination" : [ "obj-47", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-47", 0 ],
					"destination" : [ "obj-20", 0 ],
					"hidden" : 1,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-20", 0 ],
					"destination" : [ "obj-44", 4 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-13", 0 ],
					"destination" : [ "obj-11", 1 ],
					"hidden" : 1,
					"midpoints" : [ 62.5, 163.0, 267.5, 163.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-3", 0 ],
					"destination" : [ "obj-2", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-21", 0 ],
					"destination" : [ "obj-44", 5 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-32", 0 ],
					"destination" : [ "obj-44", 6 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-31", 0 ],
					"destination" : [ "obj-44", 7 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-13", 0 ],
					"destination" : [ "obj-30", 0 ],
					"hidden" : 1,
					"midpoints" : [ 62.5, 159.0, 480.5, 159.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-30", 0 ],
					"destination" : [ "obj-44", 8 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-14", 0 ],
					"destination" : [ "obj-44", 9 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-15", 0 ],
					"destination" : [ "obj-44", 10 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-8", 0 ],
					"destination" : [ "obj-44", 11 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 0 ],
					"destination" : [ "obj-19", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 2 ],
					"destination" : [ "obj-21", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 3 ],
					"destination" : [ "obj-32", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 4 ],
					"destination" : [ "obj-31", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 6 ],
					"destination" : [ "obj-14", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 7 ],
					"destination" : [ "obj-15", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-57", 8 ],
					"destination" : [ "obj-8", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-82", 1 ],
					"destination" : [ "obj-77", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-51", 0 ],
					"destination" : [ "obj-54", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-54", 0 ],
					"destination" : [ "obj-57", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-19", 0 ],
					"destination" : [ "obj-58", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-20", 0 ],
					"destination" : [ "obj-58", 1 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-21", 0 ],
					"destination" : [ "obj-58", 2 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-32", 0 ],
					"destination" : [ "obj-58", 3 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-31", 0 ],
					"destination" : [ "obj-58", 4 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-30", 0 ],
					"destination" : [ "obj-58", 5 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-14", 0 ],
					"destination" : [ "obj-58", 6 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-15", 0 ],
					"destination" : [ "obj-58", 7 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-8", 0 ],
					"destination" : [ "obj-58", 8 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-77", 0 ],
					"destination" : [ "obj-53", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-82", 0 ],
					"destination" : [ "obj-77", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-74", 0 ],
					"destination" : [ "obj-82", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-58", 0 ],
					"destination" : [ "obj-74", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-59", 0 ],
					"destination" : [ "obj-62", 0 ],
					"hidden" : 0,
					"midpoints" : [ 335.5, 457.0, 334.5, 457.0 ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-62", 0 ],
					"destination" : [ "obj-7", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
, 			{
				"patchline" : 				{
					"source" : [ "obj-2", 0 ],
					"destination" : [ "obj-59", 0 ],
					"hidden" : 0,
					"midpoints" : [  ]
				}

			}
 ]
	}

}
