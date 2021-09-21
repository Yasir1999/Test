package solver;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import grid.SudokuGrid;
import solver.DancingLinksSolver.DancingNode;


/**
 * Algorithm X solver for standard Sudoku.
 */
public class AlgorXSolver extends StdSudokuSolver
{
	
	String[][] partialSolBoard;
	String[][] originalMatrix;
	
    public AlgorXSolver() {}


    @Override
    public boolean solve(SudokuGrid grid) {

    	boolean solved = false;
    	int[][] gridBoard = grid.getGrid();
    	String[][] coverMatrix = initExactCoverGrid(gridBoard, grid);
    	Vector<Integer> rowChoices = new Vector<Integer>();
    	
    	if(solve(grid, coverMatrix, 1, rowChoices) == true) {
    		solved = true;
    	}
    	
    	int[][] resultGrid = convertPartialSolToGrid(this.partialSolBoard, grid);
    	
    	for(int i = 0; i != grid.getSize(); ++i) {
    		for(int j = 0; j != grid.getSize(); ++j) {
    			if(gridBoard[i][j] == -1) {
    				gridBoard[i][j] = resultGrid[i][j];
    			}
    		}
    	}
    	
        return solved;
    } 
    
	public boolean solve(SudokuGrid grid, String[][] coverMatrix, int level, Vector<Integer> rowChoices) {
    	boolean solved = false;
    	int gridSize = grid.getSize();
    	int[][] gridBoard = grid.getGrid();
    	int boxSize = (int) Math.sqrt(gridSize);
    	int empty = 0;
    	int constraintsNum = 4;
    	int coverStartingPos = 1;
    	int rowDelCount = 0;
    	int colDelCount = 0;
    	
    	int rowChoice = -1;
 
    	// An array to hold the sums of each column?
    	int[] columnSum = new int[constraintsNum * gridSize * gridSize];
    	SortedMap<Integer, Integer> smap = new TreeMap<Integer, Integer>();
    	
    	// Check if there is a column with a one in it
    	boolean noColumns = true;
    	for(int row = 0; row != gridSize * gridSize * gridSize; ++row) {
    		for(int col = 0; col != constraintsNum * gridSize * gridSize; ++col) {
    			if(coverMatrix[row][col] != null) {
    				if(coverMatrix[row][col].equals("1")) {
    					noColumns = false;
    					break;
    				}
    			}
    		}
    		if(noColumns == false) {
    			break;
    		}
    	}
    	
    	if(noColumns == false) {
    		this.originalMatrix = new String[coverMatrix.length][];
    		for(int i = 0; i != originalMatrix.length; ++i) {
    			this.originalMatrix[i] = Arrays.copyOf(coverMatrix[i], coverMatrix[i].length);
    		}
    	}
    	
    	if(noColumns == true) {
    		solved = true;
    		
    	}
    	
    	else {
    		
    		// Choose the first column with the lowest number of ones
    		int columnChoice = -1;
    		int sum = 0;
    		for(int column = 0; column != constraintsNum * gridSize * gridSize; ++column) {
    			for(int row = 0; row != gridSize * gridSize * gridSize; ++row) {
    				if(coverMatrix[row][column] != null) {
    					if(coverMatrix[row][column].equals("1")) {
    						sum += 1;
    					}
    				}
    			}
    			if(level <= sum) {
    				smap.put(column, sum);
    				sum = 0; 
    			}
    			sum = 0;
    		}	
    		Map.Entry<Integer, Integer> minSumValueEntry = null;
    		for(Map.Entry<Integer, Integer> sumValueEntry : smap.entrySet()) {
    			if(minSumValueEntry == null) {
    				if(sumValueEntry.getValue() >= 1) {
    					minSumValueEntry = sumValueEntry;
    				}
    			}
    			else if((sumValueEntry.getValue() < minSumValueEntry.getValue()) && sumValueEntry.getValue() >= 1) {
    				minSumValueEntry = sumValueEntry;
    			}
    		}
    		columnChoice = minSumValueEntry.getKey();
    		
    		// Choose row that contains a one in the column chosen
    		int currentLevelConsider = 0;
    		int maxLevelConsider = minSumValueEntry.getValue();
    		for(int rowConsider = 0; rowConsider != gridSize * gridSize * gridSize; ++rowConsider) {
    			if(coverMatrix[rowConsider][columnChoice] != null) {
    				if(coverMatrix[rowConsider][columnChoice].equals("1")) {
    					rowChoice = rowConsider;
    					currentLevelConsider += 1;
    					if(currentLevelConsider == level) {
    						rowChoices.add(rowChoice);
    						break;
    					}

    				}
    			}

    		}
    		
    		// Include chosen row in partial solution
    		for(int colInclude = 0; colInclude != constraintsNum * gridSize * gridSize; ++colInclude) {
    			this.partialSolBoard[rowChoice][colInclude] = coverMatrix[rowChoice][colInclude];
    		}
    		
    		for(int colj = 0; colj != constraintsNum * gridSize * gridSize; ++colj) {
    			
    			if(coverMatrix[rowChoice][colj] != null) {
    				if(coverMatrix[rowChoice][colj].equals("1")) {
    					
    					for(int rowi = 0; rowi != gridSize * gridSize * gridSize; ++rowi) {
    						
    						if(coverMatrix[rowi][colj] != null) { 
    							if(rowi != rowChoice) {
    								if(coverMatrix[rowi][colj].equals("1")) {
    									
    									// Delete row i from matrix
    									for(int i = 0; i != constraintsNum * gridSize * gridSize; ++i) {
    										coverMatrix[rowi][i] = null;
    									}

    								}
    							}
    						}
    					}
    					// Delete column j from matrix
    					for(int i = 0; i != gridSize * gridSize * gridSize; ++i) {
    						coverMatrix[i][colj] = null;
    					}

    				}
    			}
    			
    		}
    		
//    		Delete row chosen from matrix
    		for(int i = 0; i != constraintsNum * gridSize * gridSize; ++i) {
				coverMatrix[rowChoice][i] = null;
			}
    		
    		boolean noOne = true;
        	boolean invalidColumn = false;
        	int ones = 0;
        	int numZeros = 0;
        	for(int col = 0; col != gridSize * gridSize * gridSize; ++col) {
        		for(int row = 0; row != constraintsNum * gridSize * gridSize; ++row) {
        			if(coverMatrix[row][col] != null) {
        				if(coverMatrix[row][col].contains("1") == true) {
        					++ones;
        				}
        				if(coverMatrix[row][col].contains("0") == true) {
        					++numZeros;
        				}
        			}
        		}
        		if((numZeros > 0) && (ones == 0)) {
        			invalidColumn = true;
        			break;
        		}
        		else if(ones > 0) {
        			numZeros = 0;
        			ones = 0;
        		}
        	}
    		
    		
    		
    		
    		if(invalidColumn) {
    			// backtrack
    				solved = false;
    				coverMatrix = this.originalMatrix;
//    				
    				for(int i = 0; i != constraintsNum * gridSize * gridSize; ++i) {
    					this.partialSolBoard[rowChoices.lastElement()][i] = null;
    				}
    				rowChoices.remove(rowChoices.lastElement());
    				 				
    				level += 1;
    				solve(grid, coverMatrix, level, rowChoices);
    			}

    		else {
    			// continue solving on reduced matrix
    			if(solve(grid, coverMatrix, level, rowChoices) == true) {
    				solved = true;
    			}
    			
    		}
	
    	}
    	
        return solved;
    }
	
	public int[][] convertPartialSolToGrid(String[][] partialSol, SudokuGrid grid){
	
		int gridSize = grid.getSize();
		int[][] result = new int[gridSize][gridSize];
		int rowSol = 0;
		int constraintsNum = 4;
		
		for(int r = 0; r != partialSol.length; ++r) {
			for(int c = 0; c != constraintsNum * gridSize * gridSize; ++c) {
				if(partialSol[r][c] != null) {
					if(partialSol[r][c].contains("1") == true) {				
						rowSol = r;
						int row = rowSol / (gridSize * gridSize);
						int col = ((rowSol / gridSize) % gridSize);
						int val = ((rowSol % (gridSize * gridSize)) % gridSize) + 1;
						result[row][col] = val;	
						break;
					}
				}
			}
			
		}
		
		return result;
	}
    
    
    public String[][] createExactCoverGrid(SudokuGrid grid) {
    	
    	int gridSize = grid.getSize();
    	int constraintsNum = 4;
    	
        String[][] coverBoard = new String[gridSize * gridSize * gridSize][constraintsNum * gridSize * gridSize];
     
        int posSetter = 0;
        posSetter = createCellConstraint(coverBoard, posSetter, grid);
        posSetter = createRowConstraint(coverBoard, posSetter, grid);
        posSetter = createColConstraint(coverBoard, posSetter, grid);
        createBoxConstraint(coverBoard, posSetter, grid);
        
        for(int i = 0; i != gridSize * gridSize * gridSize; ++i) {
        	for(int j = 0; j != constraintsNum * gridSize * gridSize; ++j) {
        		if(coverBoard[i][j] == null) {
        			coverBoard[i][j] = "0";
        		}
        	}
        }
         
        return coverBoard;
    }
    
    // Creates exact cover grid and adds given sudoku values as solutions to the partial solution board
    public String[][] initExactCoverGrid(int[][] coverGrid, SudokuGrid grid) {
    	
    	int coverStartingPos = 1;
    	int gridSize = grid.getSize();
    	int boxSize = (int) Math.sqrt(gridSize);
    	int constraintsNum = 4;
    	int pos = -1;
    	
        String[][] coverBoard = createExactCoverGrid(grid);
        
        setPartialSolBoard(gridSize);
        
        
        
        for (int row = coverStartingPos; row <= gridSize; row++) {
	            for (int col = coverStartingPos; col <= gridSize; col++) {
	                int v = coverGrid[row - 1][col - 1];
	                if (v != -1) {
	                    for (int i = 0; i != grid.getInputNums().length; ++i) {
	                    	
	                    		int num = Integer.parseInt(grid.getInputNums()[i]);          
	                    		int n = i + 1;
	                    		if (num != v) {
	                    		}
	        					
	        					else {
	        						
									for(int column = 0; column != constraintsNum * gridSize * gridSize; ++column) {
										pos = getPos(row, col, n, grid);
										this.partialSolBoard[pos][column] = coverBoard[pos][column]; 
									}
									for(int col2 = 0; col2 != constraintsNum * gridSize * gridSize; ++col2) {
										
										if(coverBoard[pos][col2] != null) {
											if(coverBoard[pos][col2].equals("1")) {
												
												for(int row2 = 0; row2 != gridSize * gridSize * gridSize; ++row2) {
													
													if(coverBoard[row2][col2] != null && row2 != pos) {
														if(coverBoard[row2][col2].equals("1")) {
															// Delete this row from the matrix 
															for(int colDelPos = 0; colDelPos != constraintsNum * gridSize * gridSize; ++colDelPos) {
																coverBoard[row2][colDelPos] = null;
															}
														}
													}
												}
												// Delete this column from the matrix
												for(int rowDelPos = 0; rowDelPos != gridSize * gridSize * gridSize; ++rowDelPos) {
													coverBoard[rowDelPos][col2] = null;
												}
											}
										}
									}
	    						}
	                    }
	                }
	            }
	    }
        return coverBoard;
    }
    
    
    public int getPos(int row, int col, int pos, SudokuGrid grid) {
    	
    	int matrixPos = 0;
    	int gridSize = grid.getSize();
    	matrixPos = (gridSize * gridSize * (row - 1)) + (((col - 1) * gridSize) + (pos - 1));
    	
    	return matrixPos;
    }
    
    public int createCellConstraint(String[][] coverMatrix, int posSetter, SudokuGrid grid) {
    	
    	int coverStartingPos = 1;
    	int gridSize = grid.getSize();
    	int boxSize = (int) Math.sqrt(gridSize);
    	
        for (int row = coverStartingPos; row <= gridSize; ++row) {
            for (int col = coverStartingPos; col <= gridSize; ++col, ++posSetter) {
                for (int n = coverStartingPos; n <= gridSize; ++n) {
                    int pos = getPos(row, col, n, grid);
                    coverMatrix[pos][posSetter] = "1";
                }
            }
        }
        return posSetter;
    }
    
    public int createRowConstraint(String[][] coverMatrix, int posSetter, SudokuGrid grid) {
    	
    	int coverStartingPos = 1;
    	int gridSize = grid.getSize();
    	int boxSize = (int) Math.sqrt(gridSize);
    	
        for (int row = coverStartingPos; row <= gridSize; ++row) {
            for (int n = coverStartingPos; n <= gridSize; ++n, ++posSetter) {
                for (int col = coverStartingPos; col <= gridSize; ++col) {
                    int pos = getPos(row, col, n, grid);
                    coverMatrix[pos][posSetter] = "1";
                }
            }
        }
        return posSetter;
    }
    
    public int createColConstraint(String[][] coverMatrix, int posSetter, SudokuGrid grid) {
    	
    	int coverStartingPos = 1;
    	int gridSize = grid.getSize();
    	int boxSize = (int) Math.sqrt(gridSize);
    	
        for (int col = coverStartingPos; col <= gridSize; ++col) {
            for (int n = coverStartingPos; n <= gridSize; ++n, ++posSetter) {
                for (int row = coverStartingPos; row <= gridSize; ++row) {
                    int pos = getPos(row, col, n, grid);
                    coverMatrix[pos][posSetter] = "1";
                }
            }
        }
        return posSetter;
    }
    
    public int createBoxConstraint(String[][] coverMatrix, int posSetter, SudokuGrid grid) {
    	
    	int coverStartingPos = 1;
    	int gridSize = grid.getSize();
    	int boxSize = (int) Math.sqrt(gridSize);
    	
    	for (int row = coverStartingPos; row <= gridSize; row += boxSize) {
            for (int col = coverStartingPos; col <= gridSize; col += boxSize) {
                for (int n = coverStartingPos; n <= gridSize; ++n, ++posSetter) {
                    for (int rowChange = 0; rowChange < boxSize; ++rowChange) {
                        for (int columnChange = 0; columnChange < boxSize; ++columnChange) {
                            int pos = getPos(row + rowChange, col + columnChange, n, grid);
                            coverMatrix[pos][posSetter] = "1"; 
                        }
                    }
                }
            }
        }
        return posSetter;
    }
    
    private void setPartialSolBoard(int gridSize) {
    	this.partialSolBoard = new String[gridSize * gridSize * gridSize][4 * gridSize * gridSize];
    }

} 
