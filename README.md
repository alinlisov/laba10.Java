# Лабораторна робота №10. Reflection / Logging / Internationalization

## Що саме реалізовано

- **10.1. Рефлексія:** змінюється текст у **тому самому об'єкті `String`**, створеному як літерал `"Lab10ABC"`, а також у рядку, введеному з клавіатури. Виводяться значення до і після. На Java 21/26 приватне поле `String.value` має тип `byte[]`; старі Java-версії мали `char[]`. Реалізація підтримує обидва випадки, не намагаючись переприсвоювати `final`-поле.
- **10.2. Логування:** використано адаптований код банківських переказів з **попередньої лабораторної №9** (методичка дозволяє «якоїсь іншої» роботи). `java.util.logging` записує до консолі та `logs/lab10.log`. Два різні фільтри: **console INFO**, **file FINE**, тому повідомлення `FINE` потрапляє тільки у файл, а `INFO`, `WARNING`, `SEVERE` — в обидва місця.
- **10.3. Інтернаціоналізація:** додано пункт меню «Вибір мови» з `ResourceBundle`. Мови: українська, англійська, польська. Файли знаходяться саме у `resources/location`, як вимагає методичка.

## Структура

```text
Lab10_Java_Reflection_Logging_I18n/
├── src/lab10/
│   ├── Main.java
│   ├── reflection/{ReflectionDemo,UnsafeStringEditor}.java
│   ├── logging/AppLogging.java
│   ├── bank/{BankAccount,BankService,BankDemo}.java
│   └── i18n/Text.java
├── resources/location/
│   ├── messages.properties
│   ├── messages_uk.properties
│   ├── messages_en.properties
│   └── messages_pl.properties
├── test/lab10/VerificationTests.java
├── results/                    # результати перевірених запусків
├── logs/                       # створюється під час запуску
├── run_windows.bat
├── run_linux.sh
└── Звіт_ЛР10_Java.docx
```

## Як запустити в IntelliJ IDEA 

1. У панелі Project натиснути ПКМ на папці **`src` → Mark Directory as → Sources Root** (синя папка).
2. ПКМ на **`test` → Mark Directory as → Test Sources Root** (зелена).
3. ПКМ на **`resources` → Mark Directory as → Resources Root**. Інакше `ResourceBundle` не знайде переклади.
4. Відкрити `src/lab10/Main.java` → зелений трикутник → **Modify Run Configuration** або **Run → Edit Configurations → Main**.
5. В поле **VM options** ОБОВ'ЯЗКОВО ввести:

   ```
   --add-opens=java.base/java.lang=ALL-UNNAMED
   ```

   Якщо поле приховане,  **Modify options → Add VM options**.
6. (**Apply → OK**), потім запустити `Main.main()`.
7. Для тестів відкрити `test/lab10/VerificationTests.java`, запустити його через зелений трикутник і додати ту саму опцію у конфігурацію запуску тестів. Очікується `УСПІШНО: 25 із 25 перевірок`.

### Меню демонстрації

```text
1 - Рефлексія
2 - Банківські перекази та логування
3 - Вибір мови
4 - Показати файл журналу
0 - Вийти
```

1. **10.1:** пункт `1`, введи `Привіт` і `Вітаю!` (6 літер/символів в обох). Побачиш `Lab10ABC -> Changed!` та `Привіт -> Вітаю!`. Якщо рядки різної довжини чи внутрішнього кодування, програма коректно повідомить про обмеження. Мутування *інтернованого* літерала є небезпечною демонстрацією; після прикладу оригінал відновлюється.
2. **10.2:** пункт `2` — вихідна сума `1700`, кінцеві баланси `750, 650, 300`, сума після `1700`. У консолі видно `INFO`, `WARNING`, `SEVERE`; `FINE` додатково у файлі.
3. **10.3:** пункт `3` → `2` (English) → запусти пункт `2` ще раз, щоб побачити англомовне меню і логування; пункт `3` → `3` для Polski; пункт `3` → `1` повертає українську.
4. Пункт `4` показує в консолі вміст `logs/lab10.log`, доводячи наявність `FINE`.
