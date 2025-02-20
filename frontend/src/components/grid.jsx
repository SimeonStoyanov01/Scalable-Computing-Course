import { useState, useEffect } from "react";

function Grid( {gridRows, gridCols, ants} ) {
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
                                        backgroundColor: cell,
                                    }}
                                    className="grid-cell"
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