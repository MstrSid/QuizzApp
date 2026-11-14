
const { test } = require("node:test");
const fs = require("node:fs");

const {
  initializeTestEnvironment,
  assertSucceeds,
  assertFails
} = require("@firebase/rules-unit-testing");

const {
  doc,
  collection,
  getDoc,
  getDocs,
  setDoc,
  updateDoc,
  deleteDoc,
  serverTimestamp
} = require("firebase/firestore");

const TEACHER_UID = "a7C9Ht5MfUeP1785a1agXPbcs9b2";

test("Проверка безопасности Firestore для студентов и преподавателя", async () => {

  const testEnv = await initializeTestEnvironment({
    projectId: "pocketprocessingquiz",
    firestore: {
      rules: fs.readFileSync("firestore.rules", "utf8"),
      host: "127.0.0.1",
      port: 8080
    }
  });

  try {
    await testEnv.clearFirestore();

    const userA = testEnv.authenticatedContext(
      "student_A",
      {
        firebase: {
          sign_in_provider: "anonymous"
        }
      }
    ).firestore();

    const userB = testEnv.authenticatedContext(
      "student_B",
      {
        firebase: {
          sign_in_provider: "anonymous"
        }
      }
    ).firestore();

    const teacher = testEnv.authenticatedContext(
      TEACHER_UID,
      {
        firebase: {
          sign_in_provider: "password"
        }
      }
    ).firestore();

    const resultRefA = doc(
      userA,
      "test_results",
      "test_result_A"
    );

    function makeResult(overrides = {}) {
      return {
        studentName: "Тестовый студент",
        studentGroup: "TEST-01",
        testNumber: 1,
        correctAnswers: 8,
        totalQuestions: 10,
        completedAt: serverTimestamp(),
        answers: Array.from({ length: 10 }, (_, i) => ({
          questionId: i + 1,
          questionText: "Тестовый вопрос",
          options: ["Вариант 1", "Вариант 2"],
          selectedAnswer: 0,
          correctAnswer: 0,
          isCorrect: true
        })),
        userId: "student_A",
        ...overrides
      };
    }

    // 1. Студент A создаёт результат
    await assertSucceeds(
      setDoc(resultRefA, makeResult())
    );
    console.log("PASS 1: Студент A создал результат");

    // 2. Студент A читает свой результат
    await assertSucceeds(getDoc(resultRefA));
    console.log("PASS 2: Студент A читает свой результат");

    // 3. Студент B не читает результат A
    const resultRefB = doc(
      userB,
      "test_results",
      "test_result_A"
    );

    await assertFails(getDoc(resultRefB));
    console.log("PASS 3: Чтение чужого результата запрещено");

    // 4. Студент B не изменяет результат A
    await assertFails(
      updateDoc(resultRefB, {
        correctAnswers: 10
      })
    );
    console.log("PASS 4: Изменение чужого результата запрещено");

    // 5. Студент B не удаляет результат A
    await assertFails(deleteDoc(resultRefB));
    console.log("PASS 5: Удаление чужого результата запрещено");

    // 6. Подмена UID запрещена
    await assertFails(
      setDoc(
        doc(userA, "test_results", "invalid_uid"),
        makeResult({ userId: "student_B" })
      )
    );
    console.log("PASS 6: Подмена UID запрещена");

    // 7. Несуществующий номер теста запрещён
    await assertFails(
      setDoc(
        doc(userA, "test_results", "invalid_test_number"),
        makeResult({ testNumber: 5 })
      )
    );
    console.log("PASS 7: Номер теста 5 запрещён");

    // 8. Более 20 вопросов запрещено
    await assertFails(
      setDoc(
        doc(userA, "test_results", "too_many_questions"),
        makeResult({
          totalQuestions: 21,
          answers: Array.from({ length: 21 }, (_, i) => ({
            questionId: i + 1,
            questionText: "Тестовый вопрос",
            options: ["Вариант 1", "Вариант 2"],
            selectedAnswer: 0,
            correctAnswer: 0,
            isCorrect: true
          }))
        })
      )
    );
    console.log("PASS 8: Более 20 вопросов запрещено");

    // 9. Некорректный балл запрещён
    await assertFails(
      setDoc(
        doc(userA, "test_results", "invalid_correct_answers"),
        makeResult({ correctAnswers: 11 })
      )
    );
    console.log("PASS 9: Некорректный балл запрещён");

    // 10. Преподаватель читает результат студента
    const teacherResultRef = doc(
      teacher,
      "test_results",
      "test_result_A"
    );

    await assertSucceeds(getDoc(teacherResultRef));
    console.log("PASS 10: Преподаватель читает результат студента");

    // 11. Преподаватель получает список результатов
    await assertSucceeds(
      getDocs(collection(teacher, "test_results"))
    );
    console.log("PASS 11: Преподаватель получает список результатов");

    // 12. Преподаватель не изменяет результаты
    await assertFails(
      updateDoc(teacherResultRef, {
        correctAnswers: 10
      })
    );
    console.log("PASS 12: Преподаватель не может изменять результаты");

    // 13. Преподаватель не удаляет результаты
    await assertFails(deleteDoc(teacherResultRef));
    console.log("PASS 13: Преподаватель не может удалять результаты");

    // 14. Студент не получает общий список
    await assertFails(
      getDocs(collection(userA, "test_results"))
    );
    console.log("PASS 14: Студент не может получать общий список");

    console.log("\nВСЕ 14 ПРОВЕРОК ПРОЙДЕНЫ");

  } finally {
    await testEnv.cleanup();
  }
});
