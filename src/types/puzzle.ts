export type GridSize = 3 | 4 | 5;

export type GameState = 'TITLE_MENU' | 'PLAYING' | 'PAUSED' | 'VICTORY';

export type MoveDirection = 'UP' | 'DOWN' | 'LEFT' | 'RIGHT';

export interface DifficultyConfig {
  size: GridSize;
  label: string;
  title: string;
  tileCount: number;
  description: string;
  scrambleMoves: number;
}

export interface BestRecord {
  bestMoves: number;
  bestTimeSeconds: number;
}

export type RecordsBySize = Partial<Record<GridSize, BestRecord>>;

export const DIFFICULTY_OPTIONS: DifficultyConfig[] = [
  {
    size: 3,
    label: 'Relaxed',
    title: '3 × 3 Grid',
    tileCount: 8,
    description: 'Compact 8-tile board ideal for warmups and fast runs.',
    scrambleMoves: 120,
  },
  {
    size: 4,
    label: 'Classic',
    title: '4 × 4 Grid',
    tileCount: 15,
    description: 'The iconic 15-puzzle balance of strategy and speed.',
    scrambleMoves: 280,
  },
  {
    size: 5,
    label: 'Master',
    title: '5 × 5 Grid',
    tileCount: 24,
    description: 'Deep 24-tile challenge requiring row-by-row planning.',
    scrambleMoves: 520,
  },
];
