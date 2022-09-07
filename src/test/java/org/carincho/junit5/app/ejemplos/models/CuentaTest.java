package org.carincho.junit5.app.ejemplos.models;

import org.carincho.junit5.app.ejemplos.exceptions.SaldoInsuficiente;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

//Como buena practica de be ser modificador default para poder usarlo solo en el contexto del package de test,
//algo descriptivo

//@TestInstance(TestInstance.Lifecycle.PER_CLASS)//Una sola instancia por default es por metodo entonces beforeall y afterall pueden invocarse no static
class CuentaTest {

    //no es atributo que mantiene un estado es independiente para cada metodo
    Cuenta cuenta;
    private TestInfo info;
    private TestReporter reporter;

    @BeforeEach// se ejecuta despues de cada metodo
    void initMetodoTest(TestInfo info, TestReporter reporter) {

        this.info = info;
        this.reporter = reporter;
       this.cuenta = new Cuenta("Carincho", new BigDecimal("1000.12345"));
        System.out.println("Iniciando metodo");

        //la salida propia de unit de platform el sistema de unit de log
       reporter.publishEntry("Ejecutando: " + info.getDisplayName() + " " + info.getTestMethod().orElse(null).getName() + " "
                + " con las etiquetas " + info.getTags() );

    }

    @AfterEach
    void tearDown() {
        System.out.println("Finalizando el metodo de prueba");
    }


    // le pertenece a la clase puede ser para iniciar un recurso una conexion
    @BeforeAll
     static void beforeAll() {
        System.out.println("Inicializando la clase Test no la instancia");
    }


    // para finalizar un recurso
    @AfterAll
     static void afterAll() {
        System.out.println("Finalizando el test de la clase");
    }

    @Tag("cuenta")
    @Nested
    @DisplayName("Probando Atributos de Cuenta corriente")
    class CuentaTestNombreSaldo {

        @Test
        @DisplayName("nombre!!!")
        void testNombreCuenta() {

            reporter.publishEntry(info.getTags().toString());

            if(info.getTags().contains("cuenta")) {
                reporter.publishEntry("Hacer algo con la etiqueta cuenta");

            }

            cuenta = new Cuenta("Carincho", new BigDecimal("1000.12345"));//Muho mejor usar String dsi no estamos pasando un double con sus limitaciones
//        cuenta.setPersona("Carincho");
            String esperado = "Carincho";
            String real = cuenta.getPersona();

            assertNotNull(real,() ->"La cuenta no puede ser null");
            assertEquals(esperado, real, () -> "El nombre de la cuenta no es lo que se esperaba " + esperado + " Sin embargo es " + real);
            assertTrue(real.equals("Carincho"), ()-> "El nombre nombre de la cuenta debe ser a nombre de carincho");

        }

        @Test
        @DisplayName("el saldo de la cuenta que no sea null mayor que cero, y valor esperado!!!")
        void testSaldoCuenta() {

            assertNotNull(cuenta.getSaldo());
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
            assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

        }

        @Test
        @DisplayName("las referencias sean iguales !!!")
        void testReferenciaCuenta() {

            cuenta = new Cuenta("Kincho", new BigDecimal("800.997"));
            Cuenta cuenta2 = new Cuenta("Kincho", new BigDecimal("800.997"));

//        assertNotEquals(cuenta2, cuenta);
            assertEquals(cuenta2, cuenta);

        }

    }

    @Nested
    class CuentaOperacionesTest {

        @Tag("cuenta")
        @Test
        @DisplayName("Probando se debite correctamente en la cuenta!!!")
        void testDebitoCuenta() {

            cuenta = new Cuenta("Carincho", new BigDecimal("1000.50"));

            cuenta.debito(new BigDecimal("100"));

            assertNotNull(cuenta.getSaldo());
            assertEquals(900, cuenta.getSaldo().intValue());
            assertEquals("900.50", cuenta.getSaldo().toPlainString());

        }

        @Tag("cuenta")
        @Test
        @DisplayName("Probando se deposite correctamente en la cuenta!!!")
        void testCreditoCuenta() {

            cuenta = new Cuenta("Carincho", new BigDecimal("1000.50"));

            cuenta.credito(new BigDecimal("100"));

            assertNotNull(cuenta.getSaldo());
            assertEquals(1100, cuenta.getSaldo().intValue());
            assertEquals("1100.50", cuenta.getSaldo().toPlainString());

        }

        @Tag("cuenta")
        @Tag("banco")
        @Test
//    @Disabled
        @DisplayName("Probando la transferencia entre cuentas !!!")
        void testTransferirDineroCuentas() {
//        fail();
            Cuenta cuentaDestino = new Cuenta("Carincho", new BigDecimal("2500"));
            Cuenta cuentaOrigen = new Cuenta("Oscar Diaz", new BigDecimal("1500"));

            Banco banco = new Banco();

            banco.setNombre("Banco Azteca");

            banco.transferir(cuentaOrigen, cuentaDestino, new BigDecimal(500));

            assertEquals("1000", cuentaOrigen.getSaldo().toPlainString());
            assertEquals("3000", cuentaDestino.getSaldo().toPlainString());


        }

    }



    @Tag("cuenta")
    @Tag("error")
    @Test
    @DisplayName("Probando que la cuenta tenga fondos suficientes para el cargo!!!")
    void saldoInsuficienteException() {

        cuenta = new Cuenta("Carincho", new BigDecimal("1000.5"));
        Exception exception = assertThrows(SaldoInsuficiente.class, () -> {

            cuenta.debito(new BigDecimal(1500));
        });

        String actual = exception.getMessage();
        String esperado = "Saldo Insuficiente";

        assertEquals(esperado, actual);
    }



    @Tag("cuenta")
    @Tag("banco")
    @Test
    @DisplayName("Probando la relacion entre las cuenta! y el banco con assertall!!")
    void testRelacionBancoCuentas() {

        Cuenta cuentaDestino = new Cuenta("Carincho", new BigDecimal("2500"));
        Cuenta cuentaOrigen = new Cuenta("Oscar Diaz", new BigDecimal("1500"));

        Banco banco = new Banco();
        banco.addCuenta(cuentaDestino);
        banco.addCuenta(cuentaOrigen);


        banco.setNombre("Banco Azteca");

        banco.transferir(cuentaOrigen, cuentaDestino, new BigDecimal(500));

        assertAll(() -> assertEquals("1000", cuentaOrigen.getSaldo().toPlainString(), () -> "el valor de la cuenta Origen no es el esperado"),
                () -> assertEquals("3000", cuentaDestino.getSaldo().toPlainString(), () -> "el valor de la cuenta Destino no es el esperado"),
                () -> assertEquals(2, banco.getCuentas().size(), () -> "El banco no tiene el numero de cuentas esperadas"),
                () -> assertEquals("Banco Azteca", cuentaDestino.getBanco().getNombre()),
                () -> assertEquals("Carincho", banco.getCuentas().stream().filter(c -> c.getPersona().equals("Carincho"))
                        .findFirst()
                        .get().getPersona()),
                () -> assertTrue(banco.getCuentas().stream()
                        .anyMatch(c -> c.getPersona().equals("Carincho"))));//Any match si hay algun match con Carincho


//        assertTrue( banco.getCuentas().stream().filter(c->c.getPersona().equals("Carincho"))
//                .findFirst()
//                .isPresent());


    }

    @Nested
     class SistemaOperativoTest{
             @Test
             @EnabledOnOs(OS.WINDOWS)
             void testSoloWindows() {


             }

             @Test
             @EnabledOnOs({OS.LINUX, OS.MAC})
             void testSoloLinuxMac() {

             }

             @Test
             @DisabledOnOs(OS.WINDOWS)
             void testNoWindows() {
             }


     }

      @Nested
       class JavaVersionTest {
           @Test
           @EnabledOnJre(JRE.JAVA_8)
           void testSoloJDK8() {
           }

           @Test
           @EnabledOnJre(JRE.JAVA_18)
           void testSoloJDK18 () {
           }

           @Test
           @DisabledOnJre(JRE.JAVA_18)
           void testNoJDK18 () {
           }


       }

    @Nested
   class SystemPropertiesTest {

       @Test
       void imprimirSystemProperties() {

           Properties properties = System.getProperties();

           properties.forEach((k, v)-> System.out.println(k + " : " + v));
       }

       @Test
       @EnabledIfSystemProperty(named = "java.version", matches = ".*18.*")
       void testJavaVersion() {
       }

       @Test
       @DisabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
       void testSolo64() {
       }

       @Test
       @EnabledIfSystemProperty(named = "os.arch", matches = ".*32.*")
       void testNo64() {
       }

       @Test
       @EnabledIfSystemProperty(named = "user.name", matches = "oscarguillermodiaz")
       void testSoloUsuario() {
       }

       @Test
       @EnabledIfSystemProperty(named = "ENV", matches = "dev")
       void testDev() {
       }

   }

    @Nested
   class VariablesAmbienteTest {
       @Test
       void imprimirVariablesAmbiente() {

           Map<String, String> env = System.getenv();

           env.forEach((k, v) -> System.out.println(k  + " : " + v));
       }

       @Test
       @EnabledIfEnvironmentVariable(named = "JAVA_HOME", matches = "/Library/Java/JavaVirtualMachines/jdk-18.0.1.1.jdk/Contents/Home")
       void testJavaHome() {
       }

       @Test
       @EnabledIfEnvironmentVariable(named = "NUMBER_OF_PROCESSORS", matches = "12")
       void testProcesadores() {
       }

       @Test
       @EnabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "dev")
       void testEnv() {

       }

       @Test
       @DisabledIfEnvironmentVariable(named = "ENVIRONMENT", matches = "prod")
       void testEnvProdDisabled() {

       }

   }



    @Test
    @DisplayName("Probando el saldo de la cuenta que no sea null mayor que cero, y valor esperado solo cuando el ambiente sea desarrollo!!!")
    void testSaldoCuentaDev() {

        //De configuraciones del arranque
        boolean esDev = "dev".equals(System.getProperty("ENV"));


        assumeTrue(esDev);//asume que es verdadero si es falso se desactiva la prueba todo lo que esta abajo
        assertNotNull(cuenta.getSaldo());
        assertEquals(1000.12345, cuenta.getSaldo().doubleValue());
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

    }

    @Test
    @DisplayName("Saldo Cuenta dev 2")
    void testSaldoCuentaDev2() {

        //De configuraciones del arranque
        boolean esDev = "dev".equals(System.getProperty("ENV"));


        assumingThat(esDev, () -> {// esto para activar o desactivar pieza de codigo en el metodo mas pespecifico

            assertNotNull(cuenta.getSaldo());
            assertEquals(1000.12345, cuenta.getSaldo().doubleValue());

        });
        assertFalse(cuenta.getSaldo().compareTo(BigDecimal.ZERO) < 0);
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

    }

    @DisplayName("Probando Debito cuenta Rpetido")
    @RepeatedTest(value = 5, name = "{displayName} - Repeticion numero {currentRepetition} de {totalRepetitions}")
    void testDebitoCuentaRepetir(RepetitionInfo info) {

        if(info.getCurrentRepetition() == 3) {

            System.out.println("Estamos en la repeticion " + info.getCurrentRepetition());
        }
        cuenta = new Cuenta("Carincho", new BigDecimal("1000.50"));

        cuenta.debito(new BigDecimal("100"));

        assertNotNull(cuenta.getSaldo());
        assertEquals(900, cuenta.getSaldo().intValue());
        assertEquals("900.50", cuenta.getSaldo().toPlainString());

    }

    static List<String> montoList() {

        return Arrays.asList("100", "200", "300", "500", "700", "1000.12345");

    }
    @Tag("param")
    @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} {argumentsWithNames} ")
    @MethodSource("montoList")
    void testDebitoCuentaParametrizadoMethodSource(String monto) {

        cuenta.debito(new BigDecimal(monto));

        assertNotNull(cuenta.getSaldo());
        assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);
    }

    @Tag("param")
    @Nested
    class PruebasParametrizadasTest {
        @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} {argumentsWithNames} ")
        @ValueSource(strings = {"100", "200", "300", "500", "700", "1000.12345"})
        void testDebitoCuentaParametrizadoValueSource(String monto) {

            cuenta.debito(new BigDecimal(monto));

            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);


        }

        @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} {argumentsWithNames} ")
        @CsvSource({"1,100", "2,200", "3,300", "4,500", "5,700", "6,1000.12345"})
        void testDebitoCuentaParametrizadoCsvSource(String index, String monto) {

            System.out.println(index + " -> " + monto);
            cuenta.debito(new BigDecimal(monto));

            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);


        }

        @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} {argumentsWithNames} ")
        @CsvFileSource(resources = "/data.csv")
        void testDebitoCuentaParametrizadoFileCsvSource(String monto) {

            cuenta.debito(new BigDecimal(monto));

            assertNotNull(cuenta.getSaldo());
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);


        }




        @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} {argumentsWithNames} ")
        @CsvSource({"200,100, Carincho, Carincho", "250,200, carincho men, Carincho", "300,300, kincho, kincho", "510,500, Oscar, Oscar", "750,700, memo, Memo", "1000.12345,1000.12345, kalimbaba, kalimbaba"})
        void testDebitoCuentaParametrizadoCsvSource2(String saldo, String monto, String esperado, String actual) {

            System.out.println(saldo + " -> " + monto);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);


        }

        @ParameterizedTest(name = "Numero {index} ejecutando con valor {0} {argumentsWithNames} ")
        @CsvFileSource(resources = "/data2.csv")
        void testDebitoCuentaParametrizadoFileCsvSource2(String saldo, String monto, String esperado, String actual) {

            System.out.println(saldo + " -> " + monto);
            cuenta.setSaldo(new BigDecimal(saldo));
            cuenta.debito(new BigDecimal(monto));
            cuenta.setPersona(actual);

            assertNotNull(cuenta.getSaldo());
            assertNotNull(cuenta.getPersona());
            assertEquals(esperado, actual);
            assertTrue(cuenta.getSaldo().compareTo(BigDecimal.ZERO) > 0);

        }
    }

    @Nested
    @Tag("timeout")
    class EjemploTimeOutTest {
        @Test
        @Timeout(1)
        void testTimeOut() throws InterruptedException {

            TimeUnit.MILLISECONDS.sleep(100);
        }

        @Test
        @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)//por defecto es en segudos
        void testTimeOut2() throws InterruptedException {

            TimeUnit.MILLISECONDS.sleep(900);
        }

        @Test
        void testTimeoutAssertions() {

            assertTimeout(Duration.ofSeconds(5), () -> {

                TimeUnit.MILLISECONDS.sleep(4000);

            });
        }
    }

}