import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class StrassenMatrixMultiplicationTest {

    private StrassenMatrixMultiplication smm;

    // --- 生命周期方法 (Life Cycle Methods) ---

    @BeforeAll
    static void beforeAll() {
        System.out.println("**--- StrassenMatrixMultiplicationTest 开始执行 ---**");
    }

    @BeforeEach
    void setUp() {
        System.out.println("--- Executed before each test method ---");
        // 每次测试前，实例化 StrassenMatrixMultiplication 对象
        smm = new StrassenMatrixMultiplication();
    }

    @AfterEach
    void tearDown() {
        System.out.println("--- Executed after each test method ---");
        // 释放实例引用
        smm = null;
    }

    @AfterAll
    static void afterAll() {
        System.out.println("**--- StrassenMatrixMultiplicationTest 执行完毕 ---**");
    }

    // --- 核心断言辅助方法 (Core Assertion Helper) ---

    /**
     * 对二维矩阵进行深层比较，确保每一行都相等
     * @param expected 预期矩阵
     * @param actual 实际矩阵
     */
    private void assertMatrixDeepEquals(int[][] expected, int[][] actual) {
        // 1. 检查矩阵是否为空
        assertNotNull(expected, "预期矩阵不应为 null");
        assertNotNull(actual, "实际矩阵不应为 null");

        // 2. 检查行数是否相等
        assertEquals(expected.length, actual.length, "矩阵行数不匹配");

        // 3. 逐行进行比较
        for (int i = 0; i < expected.length; i++) {
            // 对每一行（一维数组）使用标准的 assertArrayEquals()
            assertArrayEquals(expected[i], actual[i], "矩阵第 " + i + " 行数据不匹配");
        }
    }


    // --- 辅助方法测试 (Helper Methods Tests) ---

    @DisplayName("Test Matrix Addition (add)")
    @Test
    void add() {
        System.out.println("--- Test add executed ---");
        int[][] A = {{1, 2}, {3, 4}};
        int[][] B = {{5, 6}, {7, 8}};
        int[][] expected = {{6, 8}, {10, 12}};

        int[][] result = smm.add(A, B);
        assertMatrixDeepEquals(expected, result);
    }

    @DisplayName("Test Matrix Subtraction (sub)")
    @Test
    void sub() {
        System.out.println("--- Test sub executed ---");
        int[][] A = {{10, 20}, {30, 40}};
        int[][] B = {{1, 2}, {3, 4}};
        int[][] expected = {{9, 18}, {27, 36}};

        int[][] result = smm.sub(A, B);
        assertMatrixDeepEquals(expected, result);
    }

    @DisplayName("Test Matrix Splitting (split)")
    @Test
    void split() {
        System.out.println("--- Test split executed ---");
        int[][] P = {{1, 2, 3, 4}, {5, 6, 7, 8}, {9, 10, 11, 12}, {13, 14, 15, 16}};
        int halfN = 2;
        int[][] C11 = new int[halfN][halfN]; // Top-Left

        // 测试左上角 (0, 0)
        smm.split(P, C11, 0, 0);
        int[][] expectedC11 = {{1, 2}, {5, 6}};
        assertMatrixDeepEquals(expectedC11, C11);

        int[][] C22 = new int[halfN][halfN]; // Bottom-Right
        // 测试右下角 (n/2, n/2)
        smm.split(P, C22, halfN, halfN);
        int[][] expectedC22 = {{11, 12}, {15, 16}};
        assertMatrixDeepEquals(expectedC22, C22);
    }

    @DisplayName("Test Matrix Joining (join)")
    @Test
    void join() {
        System.out.println("--- Test join executed ---");
        int N = 4;
        int halfN = 2;
        int[][] P = new int[N][N]; // 父矩阵
        int[][] C = {{10, 20}, {30, 40}}; // 子矩阵

        // 将子矩阵 C 组合到 P 的右下角 (halfN, halfN)
        smm.join(C, P, halfN, halfN);

        int[][] expectedP = {
                {0, 0, 0, 0},
                {0, 0, 0, 0},
                {0, 0, 10, 20},
                {0, 0, 30, 40}
        };
        assertMatrixDeepEquals(expectedP, P);
    }

    // --- 核心方法测试 (Core Method Tests) ---

    @DisplayName("Test Strassen Multiplication (1x1 Base Case)")
    @Test
    void multiply_1x1() {
        System.out.println("--- Test multiply 1x1 executed ---");
        int[][] A = {{5}};
        int[][] B = {{3}};
        int[][] expected = {{15}};

        int[][] result = smm.multiply(A, B);
        assertMatrixDeepEquals(expected, result);
    }

    @DisplayName("Test Strassen Multiplication (2x2)")
    @Test
    void multiply_2x2() {
        System.out.println("--- Test multiply 2x2 executed ---");
        // 预期结果：{{1*5+2*7, 1*6+2*8}, {3*5+4*7, 3*6+4*8}} = {{19, 22}, {43, 50}}
        int[][] A = {{1, 2}, {3, 4}};
        int[][] B = {{5, 6}, {7, 8}};
        int[][] expected = {{19, 22}, {43, 50}};

        int[][] result = smm.multiply(A, B);
        assertMatrixDeepEquals(expected, result);
    }

    @DisplayName("Test Strassen Multiplication (4x4)")
    @Test
    void multiply_4x4() {
        System.out.println("--- Test multiply 4x4 executed ---");
        // A = all 1s, B = Identity Matrix -> Result should be A
        int[][] A = {
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1}
        };
        int[][] B = {
                {1, 0, 0, 0},
                {0, 1, 0, 0},
                {0, 0, 1, 0},
                {0, 0, 0, 1}
        };
        int[][] expected = {
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1},
                {1, 1, 1, 1}
        };

        int[][] result = smm.multiply(A, B);
        assertMatrixDeepEquals(expected, result);
    }
}