import Link from "next/link";
import styles from "./page.module.css";

export default function Home() {
  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <Link href={"/movies"}>See all movies</Link>
      </main>
      <footer className={styles.footer}>
        footer content here
      </footer>
    </div>
  );
}
