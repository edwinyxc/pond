package pond.core;

public interface PondAware
{
    /**
     * Set Environment for spi
     */
    void pond(Pond pond);
    
    /**
     * Get Environment
     */
    Pond pond();
}
