import { useState, useEffect } from "react";
import ant_icon from '../ant_icon.png';

function Grid( {gridRows, gridCols, updates} ) {
    const [grid, setGrid] = useState([]);

    useEffect(() => {
        const newGrid = Array.from({ length: gridRows }, () =>
          Array.from({ length: gridCols }, () => ({
            colour: "white",
            ants: [],
          }))
        );
        setGrid(newGrid);
      }, [gridRows, gridCols])

    useEffect(() => {
        if (!updates) return;
        if (!updates.cells) return;
        
        setGrid((oldGrid) => {
          const newGrid = oldGrid.map((row) => 
            row.map((cell) => ({ ...cell }))
          );
    
          updates.cells.forEach((cellUpdate) => {
            const { rowInd, colInd, colour, ants } = cellUpdate;
            newGrid[rowInd][colInd].color = colour ? "grey" : "white";
            newGrid[rowInd][colInd].ants = ants;
          });
    
          return newGrid;
        });
      }, [updates]); 

      function getRotation(direction) {
        switch (direction) {
          case "North":
            return 0;
          case "East":
            return 90;
          case "South":
            return 180;
          case "West":
            return 270;
          default:
            return 0;
        }
      }
    
      return (
        <div className="grid-container">
          <table className="grid-table">
            <tbody>
              {grid.map((row, rowIndex) => (
                <tr key={rowIndex}>
                  {row.map((cell, colIndex) => (
                    <td 
                      key={colIndex}
                      style={{ backgroundColor: cell.color }}
                      className="grid-cell"
                    >
                      {
                        cell.ants.map((ant, idx) => {
                            const rotation = getRotation(ant.direction);
                            return(
                            <div key={idx} className="ant-container">
                            <img
                              src={ant_icon}
                              alt="ant"
                              style={{
                                position: 'absolute',
                                top: "50%",
                                left: "50%",
                                transform: `translate(-50%, -50%) rotate(${rotation}deg)`,
                                width: "35%",
                                height: "auto",
                                objectFit: "contain",
                              }}
                            />
                            {/* {ant.direction} */}
                          </div>
                        );})
                      }
                    </td>
                  ))}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      );
    }

export default Grid;