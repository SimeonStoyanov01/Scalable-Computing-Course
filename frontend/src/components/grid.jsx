import { useState, useEffect } from "react";

function Grid(gridRows, gridCols) {
    const [grid, setGrid] = useState([]);

    useEffect(() => {
        const newGrid = Array.from({ length: gridRows }, () =>
          Array.from({ length: gridCols }, () => "white")
        );
        setGrid(newGrid);
      }, [gridRows, gridCols]);
    
    return (
      <div className="grid-container">
        <table className="grid-table">
          <tbody>
            {grid.map((row, rowIndex) => (
              <tr key={rowIndex}>
                {row.map((cell, colIndex) => (
                  <td 
                    key={colIndex}
                    style={{
                      width: "20px",
                      height: "20px",
                      backgroundColor: cell,
                      border: "1px solid black", 
                    }}
                  ></td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    );
}

export default Grid;