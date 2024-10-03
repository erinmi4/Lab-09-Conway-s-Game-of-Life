package gameoflife;
import edu.princeton.cs.algs4.StdDraw;
import tileengine.TERenderer;
import tileengine.TETile;
import tileengine.Tileset;
import utils.FileUtils;
import java.awt.event.KeyEvent;
import java.util.Random;

/**
 * Am implementation of Conway's Game of Life using StdDraw.
 * Credits to Erik Nelson, Jasmine Lin and Elana Ho for
 * creating the assignment.
 */
public class GameOfLife {

    private static final int DEFAULT_WIDTH = 50;
    private static final int DEFAULT_HEIGHT = 50;
    private static final String SAVE_FILE = "src/save.txt";
    private long prevFrameTimestep;
    private TERenderer ter;
    private Random random;
    private TETile[][] currentState;
    private int width;
    private int height;

    /**
     * Initializes our world.
     * @param seed
     */
    public GameOfLife(long seed) {
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        ter = new TERenderer();
        ter.initialize(width, height);
        random = new Random(seed);
        TETile[][] randomTiles = new TETile[width][height];
        fillWithRandomTiles(randomTiles);
        currentState = randomTiles;
    }

    /**
     * Constructor for loading in the state of the game from the
     * given filename and initializing it.
     * @param filename
     */
    public GameOfLife(String filename) {
        this.currentState = loadBoard(filename);
        ter = new TERenderer();
        ter.initialize(width, height);
    }

    /**
     * Constructor for loading in the state of the game from the
     * given filename and initializing it. For testing purposes only, so
     * do not modify.
     * @param filename
     */
    public GameOfLife(String filename, boolean test) {
        this.currentState = loadBoard(filename);
    }

    /**
     * Initializes our world without using StdDraw. For testing purposes only,
     * so do not modify.
     * @param seed
     */
    public GameOfLife(long seed, boolean test) {
        width = DEFAULT_WIDTH;
        height = DEFAULT_HEIGHT;
        random = new Random(seed);
        TETile[][] randomTiles = new TETile[width][height];
        fillWithRandomTiles(randomTiles);
        currentState = randomTiles;
    }

    /**
     * Initializes our world with a given TETile[][] without using StdDraw.
     * For testing purposes only, so do not modify.
     * @param tiles
     * @param test
     */
    public GameOfLife(TETile[][] tiles, boolean test) {
        TETile[][] transposeState = transpose(tiles);
        this.currentState = flip(transposeState);
        this.width = tiles[0].length;
        this.height = tiles.length;
    }

    /**
     * Flips the matrix along the x-axis.
     * @param tiles
     * @return
     */
    private TETile[][] flip(TETile[][] tiles) {
        int w = tiles.length;
        int h = tiles[0].length;

        TETile[][] rotateMatrix = new TETile[w][h];
        int y = h - 1;
        for (int j = 0; j < h; j++) {
            for (int i = 0; i < w; i++) {
                rotateMatrix[i][y] = tiles[i][j];
            }
            y--;
        }
        return rotateMatrix;
    }

    /**
     * Transposes the tiles.
     * @param tiles
     * @return
     */
    private TETile[][] transpose(TETile[][] tiles) {
        int w = tiles[0].length;
        int h = tiles.length;

        TETile[][] transposeState = new TETile[w][h];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                transposeState[x][y] = tiles[y][x];
            }
        }
        return transposeState;
    }

    /**
     * Runs the game. You don't have to worry about how this method works.
     * DO NOT MODIFY THIS METHOD!
     */
    public void runGame() {
        boolean paused = false;
        long evoTimestamp = System.currentTimeMillis();
        long pausedTimestamp = System.currentTimeMillis();
        long clickTimestamp = System.currentTimeMillis();
        while (true) {
            if (!paused && System.currentTimeMillis() - evoTimestamp > 250) {
                evoTimestamp = System.currentTimeMillis();
                currentState = nextGeneration(currentState);
            }
            if (System.currentTimeMillis() - prevFrameTimestep > 17) {
                prevFrameTimestep = System.currentTimeMillis();

                double mouseX = StdDraw.mouseX();
                double mouseY = StdDraw.mouseY();
                int tileX = (int) mouseX;
                int tileY = (int) mouseY;

                TETile currTile = currentState[tileX % width][tileY % height];

                if (StdDraw.isMousePressed() && System.currentTimeMillis() - clickTimestamp > 250) {
                    clickTimestamp = System.currentTimeMillis();
                    if (currTile == Tileset.CELL) {
                        currentState[tileX][tileY] = Tileset.NOTHING;
                    } else {
                        currentState[tileX][tileY] = Tileset.CELL;
                    }
                }
                if (StdDraw.isKeyPressed(KeyEvent.VK_SPACE) && System.currentTimeMillis() - pausedTimestamp > 500) {
                    pausedTimestamp = System.currentTimeMillis();
                    paused = !paused;
                }
                if (StdDraw.isKeyPressed(KeyEvent.VK_S)) {
                    saveBoard();
                    System.exit(0);
                }
                ter.renderFrame(currentState);
            }
        }
    }


    /**
     * Fills the given 2D array of tiles with RANDOM tiles.
     * @param tiles
     */
    public void fillWithRandomTiles(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = randomTile();
            }
        }
    }

    /**
     * Fills the 2D array of tiles with NOTHING tiles.
     * @param tiles
     */
    public void fillWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    /**
     * Selects a random tile, with a 50% change of it being a CELL
     * and a 50% change of being NOTHING.
     */
    private TETile randomTile() {
        // The following call to nextInt() uses a bound of 3 (this is not a seed!) so
        // the result is bounded between 0, inclusive, and 3, exclusive. (0, 1, or 2)
        int tileNum = random.nextInt(2);
        return switch (tileNum) {
            case 0 -> Tileset.CELL;
            default -> Tileset.NOTHING;
        };
    }

    /**
     * Returns the current state of the board.
     * @return
     */
    public TETile[][] returnCurrentState() {
        return currentState;
    }

    /**
     * At each timestep, the transitions will occur based on the following rules:
     *  1.Any live cell with fewer than two live neighbors dies, as if by underpopulation.
     *  2.Any live cell with two or three neighbors lives on to the next generation.
     *  3.Any live cell with more than three neighbors dies, as if by overpopulation,
     *  4.Any dead cell with exactly three live neighbors becomes a live cell, as if by reproduction.
     * @param tiles
     * @return
     * 对于规则的使用，不是按照一个一个来的，而是一次就做出了判断
     */
public TETile[][] nextGeneration(TETile[][] tiles) {
    TETile[][] nextGen = new TETile[width][height];
    TETile[][] curGen = tiles;

    // 避免重复计算 liveCellCount
    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height ; y++) {
            int liveNeighbors = liveCellCount(x, y, curGen);

            // 当前是活细胞
            if (curGen[x][y] == Tileset.CELL) {
                if (liveNeighbors < 2 || liveNeighbors > 3) {
                    nextGen[x][y] = Tileset.NOTHING;  // 死亡
                } else {
                    nextGen[x][y] = Tileset.CELL;  // 继续存活
                }
            }
            // 当前是死细胞
            else {
                if (liveNeighbors == 3) {
                    nextGen[x][y] = Tileset.CELL;  // 复活
                } else {
                    nextGen[x][y] = Tileset.NOTHING;  // 继续保持死亡
                }
            }
        }
    }
//打印板子，判断预期
//    nextGen = flip(nextGen);
//    nextGen = rotate90Degrees(nextGen);
//    nextGen = rotate90Degrees(nextGen);
//    nextGen = rotate90Degrees(nextGen);
    printBoard(nextGen);

    return nextGen;
}


    /**
     * 实现对于一个坐标的valid判断*/
public boolean checkSiteValid(int row, int col) {
    return row >= 0 && row < width && col >= 0 && col < height;
}

    /**
     * 实现一个判断周围有多少个存活cell的函数.
     * @Parameter row 行坐标X
     * @Parameter col  列坐标Y
     * 如果传入的是一个不存在的点，会返回-1，
     * 否则应该返回的值为[0,1,2,3,4]
     * */
public int liveCellCount(int row, int col, TETile[][] curGen) {
    if (!checkSiteValid(row, col)) return -1;

    int sum = 0;

    int[] rowOffsets = {-1, -1, -1, 0, 0, 1, 1, 1};
    int[] colOffsets = {-1, 0, 1, -1, 1, -1, 0, 1};

    for (int i = 0; i < 8; i++) {
        int newRow = row + rowOffsets[i];
        int newCol = col + colOffsets[i];

        if (newRow >= 0 && newRow < curGen.length && newCol >= 0 && newCol < curGen[0].length) {
            if (curGen[newRow][newCol] == Tileset.CELL) {
                sum++;
            }
        }
    }

    // 调试输出，查看每次计算的活细胞数
    //System.out.println("Live cells around (" + row + ", " + col + "): " + sum);

    return sum;
}


    /**
     * Helper method for saveBoard without rendering and running the game.
     * @param tiles
     */
    public void saveBoard(TETile[][] tiles) {
        TETile[][] transposeState = transpose(tiles);
        this.currentState = flip(transposeState);
        this.width = tiles[0].length;
        this.height = tiles.length;
        saveBoard();
    }

    /**
     * Saves the state of the current state of the board into the
     * save.txt file (make sure it's saved into this specific file).
     * 0 represents NOTHING, 1 represents a CELL.
     */
public void saveBoard() {
    // 获取当前的棋盘状态
    TETile[][] curGen = returnCurrentState();

    FileUtils newsave = new FileUtils();

    // 如果文件不存在，抛出错误（也可以改为创建新文件）
    if (!newsave.fileExists(SAVE_FILE)) {
        throw new IllegalArgumentException("File does not exist");
    }

    // 写入棋盘的维度（宽度和高度），并以换行符结束
    StringBuilder boardState = new StringBuilder();
    boardState.append(width).append(" ").append(height).append("\n");

    // 使用行优先顺序遍历棋盘，将状态存入 map
    // 注意，遍历行时 y 从 0 到 height - 1，表示从下往上遍历
    for (int y = height - 1; y >= 0; y--) {  // 从最后一行开始遍历
        for (int x = 0; x < width; x++) {  // 遍历每一列
            if (curGen[x][y] == Tileset.CELL) {
                boardState.append("1");
            } else {
                boardState.append("0");
            }
        }
        boardState.append("\n");  // 每行结束后添加换行符
    }

    // 将所有内容一次性写入文件
    newsave.writeFile(SAVE_FILE, boardState.toString());
}


    /**
     * Loads the board from filename and returns it in a 2D TETile array.
     * 0 represents NOTHING, 1 represents a CELL.
     */
public TETile[][] loadBoard(String filename) {
    FileUtils newsave = new FileUtils();

    // 如果文件不存在，抛出错误
    if (!newsave.fileExists(filename)) {
        throw new IllegalArgumentException("File does not exist");
    }

    // 读取文件内容并根据换行符切割成行
    String boardState = newsave.readFile(filename).toString();
    String[] map = boardState.split("\\R");

    // 从第一行提取棋盘的维度
    int width = Integer.parseInt(map[0].split(" ")[0]);
    int height = Integer.parseInt(map[0].split(" ")[1]);

    // 创建一个二维数组来加载棋盘状态
    TETile[][] loads = new TETile[width][height];

    // 从文件的第二行开始加载棋盘状态，逐个字符处理每一行
    for (int y = 0; y < height; y++) {
        String row = map[y + 1];  // 每一行作为一个字符串
        for (int x = 0; x < width; x++) {
            char tileChar = row.charAt(x);  // 逐个字符处理
            if (tileChar == '1') {
                loads[x][y] = Tileset.CELL;  // 如果是 '1'，表示这个位置是活细胞
            } else if (tileChar == '0') {
                loads[x][y] = Tileset.NOTHING;  // 如果是 '0'，表示这个位置是空的
            }
        }
    }

    // 返回加载的棋盘
    return loads;
}

    /*沿着x轴水平翻转*/
public TETile[][] flipVertically(TETile[][] tiles) {
    int width = tiles.length;
    int height = tiles[0].length;

    TETile[][] flipped = new TETile[width][height];

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            flipped[x][y] = tiles[x][height - 1 - y];  // 将第y行和倒数第y行互换
        }
    }

    return flipped;
}

/*将板子旋转90°*/
public TETile[][] rotate90Degrees(TETile[][] board) {
    int width = board.length;
    int height = board[0].length;
    TETile[][] rotated = new TETile[height][width];

    for (int x = 0; x < width; x++) {
        for (int y = 0; y < height; y++) {
            rotated[y][width - 1 - x] = board[x][y];
        }
    }

    return rotated;
}

    /*
* 将板子打印出来*/
public void printBoard(TETile[][] board) {
    for (int x = 0; x < board.length; x++) {  // 遍历每一行
        for (int y = 0; y < board[0].length; y++) {  // 遍历每一列
            if (board[x][y] == Tileset.CELL) {
                System.out.print("1");  // 用 '1' 表示活细胞
            } else if (board[x][y] == Tileset.NOTHING) {
                System.out.print("0");  // 用 '0' 表示空细胞
            }
        }
        System.out.println();  // 打印一行后换行
    }
}

    /**
     * This is where we run the program. DO NOT MODIFY THIS METHOD!
     * @param args
     */
    public static void main(String[] args) {
        if (args.length == 2) {
            // Read in the board from a file.
            if (args[0].equals("-l")) {
                GameOfLife g = new GameOfLife(args[1]);
                g.runGame();
            }
            System.out.println("Verify your program arguments!");
            System.exit(0);
        } else {
            long seed = args.length > 0 ? Long.parseLong(args[0]) : (new Random()).nextLong();
            GameOfLife g = new GameOfLife(seed);
            g.runGame();
        }
    }
}


