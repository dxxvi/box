#[cfg(test)]
mod tests {
    use books_rust::init_logger;
    use books_rust::utils::{Dog, Fish};
    use log::info;

    #[test]
    fn test_something() {
        init_logger();

        let dog1 = Dog {
            name: String::from("Dog 1"),
            age: 3,
        };
        let dog2 = Dog {
            name: String::from("Dog 2"),
            age: 19,
        };
        info!(
            "{:?} has this json string {:?}",
            dog1,
            serde_json::to_string(&dog1)
        );

        let my_fish = Fish {
            name: String::from("Fish 1"),
            heavy: None,
            dogs: vec![dog1, dog2],
        };
        let pretty_string = serde_json::to_string_pretty(&my_fish).unwrap();
        info!("{:?} has this json string {}", &my_fish, &pretty_string);
    }
}
