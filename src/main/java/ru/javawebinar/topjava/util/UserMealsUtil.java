package ru.javawebinar.topjava.util;

import ru.javawebinar.topjava.model.UserMeal;
import ru.javawebinar.topjava.model.UserMealWithExcess;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.*;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class UserMealsUtil {
    public static void main(String[] args) {
        List<UserMeal> meals = Arrays.asList(
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 10, 0), "Завтрак", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 13, 0), "Обед", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 30, 20, 0), "Ужин", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 0, 0), "Еда на граничное значение", 100),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 10, 0), "Завтрак", 1000),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 13, 0), "Обед", 500),
                new UserMeal(LocalDateTime.of(2020, Month.JANUARY, 31, 20, 0), "Ужин", 410)
        );

        List<UserMealWithExcess> mealsTo = filteredByCycles(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo.forEach(System.out::println);

        List<UserMealWithExcess> mealsTo2 = filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000);
        mealsTo2.forEach(System.out::println);
//        System.out.println(filteredByStreams(meals, LocalTime.of(7, 0), LocalTime.of(12, 0), 2000));
    }

    public static class UserMealMy {
        List <UserMeal> userMeals = new ArrayList<>();
        List <UserMealWithExcess> userMealsByFilter = new ArrayList<>();
        int caloriesPerDay = 0;
        Excess excess;
    }
    public static List<UserMealWithExcess> filteredByCycles(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
        Map<LocalDate,UserMealMy> mealPerDay = new HashMap<>();
        for (UserMeal userMeal:meals) {
            UserMealMy userMealMy = mealPerDay.get(userMeal.getDateTime().toLocalDate());
            if (userMealMy == null)
                userMealMy = new UserMealMy();
            if (userMealMy.excess == null) {
                userMealMy.excess = new Excess(false);
            }
            userMealMy.userMeals.add(userMeal);
            userMealMy.caloriesPerDay = userMealMy.caloriesPerDay + userMeal.getCalories();
            if (userMealMy.caloriesPerDay > caloriesPerDay)
                userMealMy.excess.setExcess(true);
            if (userMeal.getDateTime().toLocalTime().isAfter(startTime) && userMeal.getDateTime().toLocalTime().isBefore(endTime))
                userMealMy.userMealsByFilter.add(new UserMealWithExcess(userMeal.getDateTime(),userMeal.getDescription(),userMeal.getCalories(),userMealMy.excess));
            mealPerDay.put(userMeal.getDateTime().toLocalDate(),userMealMy);
        }
        List<UserMealWithExcess> result = new ArrayList<>();
        for (Map.Entry<LocalDate, UserMealMy> pair: mealPerDay.entrySet())
        {
            result.addAll(pair.getValue().userMealsByFilter);
        }
        return result;
    }

    public static List<UserMealWithExcess> filteredByStreams(List<UserMeal> meals, LocalTime startTime, LocalTime endTime, int caloriesPerDay) {
      //  Map<LocalDate,UserMealMy> mealPerDay  =  meals.stream().collect(Collectors.toMap(p ->p.g)).// TODO Implement by streams
       /// Map<LocalDate,List <UserMeal>> mealPerDay  =
        Map<LocalDate, Integer> collect = meals.stream().collect(Collectors.groupingBy(m -> m.getDateTime().toLocalDate(), Collectors.summingInt(um -> um.getCalories())));
        return meals.stream().filter(m->TimeUtil.isBetweenHalfOpen(m.getDateTime().toLocalTime(),startTime,endTime))
                .map(m -> new UserMealWithExcess(m.getDateTime(),m.getDescription(),m.getCalories(),new Excess(Boolean.valueOf(collect.get(m.getDateTime().toLocalDate())>caloriesPerDay))))
                .collect(Collectors.toList());

    }
}
